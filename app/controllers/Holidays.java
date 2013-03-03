package controllers;

import be.objectify.deadbolt.actions.And;
import be.objectify.deadbolt.actions.Restrictions;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import constants.HalfDayType;
import constants.MissionType;
import constants.Pattern;
import dto.holiday.HolidayDTO;
import dto.holiday.HolidayDataTableDTO;
import helpers.ErrorMsgFactory;
import helpers.SecurityHelper;
import helpers.deserializer.DateTimeDeserializer;
import helpers.deserializer.ObjectIdDeserializer;
import helpers.formatter.JodaDateTimeFormatter;
import helpers.serializer.ObjectIdSerializer;
import helpers.time.TimerHelper;
import mailer.Mailer;
import models.*;
import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import play.data.Form;
import play.libs.Akka;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import security.RoleName;
import security.Secured;
import views.html.holidays.show;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static play.libs.Json.toJson;

/**
 * User: LPU
 * Date: 27/08/12
 * Time: 16:09
 */
@Security.Authenticated(Secured.class)
@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
public class Holidays extends AbstractCraController {

	private static abstract class AbstractHolidayForm {

		public String trigramme;

		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		public ObjectId holidayMissionId;

		public String comment;
	}

	public static class HolidayOneDayForm extends AbstractHolidayForm {

		@JsonDeserialize(using = DateTimeDeserializer.class)
		@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
		public DateTime date;

		public Boolean morning;

		public Boolean afternoon;

		public String validate() {
			//Si le jour saisie est un DayOff
			if (TimerHelper.isDayOff(date) || TimerHelper.isSaturdayOrSunday(date)) {
				return "Veuillez saisir un jour ouvré.";
			}
			return null;
		}
	}

	public static class HolidayPeriodForm extends AbstractHolidayForm {
		@JsonDeserialize(using = DateTimeDeserializer.class)
		@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
		public DateTime startHoliday;
		@JsonDeserialize(using = DateTimeDeserializer.class)
		@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
		public DateTime endHoliday;

		public Boolean startMorning;

		public Boolean endAfternoon;

		public String validate() {
			if (TimerHelper.containsOnlyDayOffInPeriod(startHoliday, endHoliday)) {
				return "Veuillez saisir des jours ouvrés.";
			}
			return null;
		}
	}

	public static class CheckHolidayForm extends AbstractHolidayForm {
		/* On day */
		@JsonDeserialize(using = DateTimeDeserializer.class)
		@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
		public DateTime date;

		public Boolean morning;

		public Boolean afternoon;

		/* Period*/
		@JsonDeserialize(using = DateTimeDeserializer.class)
		@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
		public DateTime startHoliday;
		@JsonDeserialize(using = DateTimeDeserializer.class)
		@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
		public DateTime endHoliday;

		public Boolean startMorning;

		public Boolean endAfternoon;
	}

	public static class HolidayRecurrentForm extends AbstractHolidayForm {
		@JsonProperty
		public Integer repeatEveryXWeeks;
		@JsonProperty
		public List<Integer> daysOfWeek = Lists.newArrayList();
		@JsonDeserialize(using = DateTimeDeserializer.class)
		@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
		public DateTime startHoliday;
		@JsonProperty
		public String endRecurrence;
		@JsonProperty
		public Integer nbOfOccurences;
		@JsonDeserialize(using = DateTimeDeserializer.class)
		@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
		public DateTime endHoliday;

		public Boolean morning;
		public Boolean afternoon;

		public String validate() {
			return null;
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(this)/**/
					.add("repeatEveryXWeeks", repeatEveryXWeeks)/**/
					.add("daysOfWeek", daysOfWeek)/**/
					.add("startHoliday", startHoliday)/**/
					.add("endRecurrence", endRecurrence)/**/
					.add("nbOfOccurences", nbOfOccurences)/**/
					.add("endHoliday", endHoliday)/**/
					.toString();
		}
	}

	public static class HolidayDeleteForm extends HolidayOneDayForm {
		@JsonDeserialize(using = DateTimeDeserializer.class)
		@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
		public DateTime startHoliday;
		@JsonDeserialize(using = DateTimeDeserializer.class)
		@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
		public DateTime endHoliday;

		public String validate() {
			return null;
		}
	}

	/**
	 * Show Create Holiday page
	 */
	public static Result show() {
		final User connectedUser = SecurityHelper.getConnectedUser();
		final List<Employee> employees = loadEmployees(connectedUser.role.getRoleName());
		final List<Mission> holidayType = Mission.findByType(MissionType.HOLIDAY);
		return ok(show.render(employees, holidayType, connectedUser));
	}

	/**
	 * Create one holiday
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public static Result createOneDayHoliday() {
		final Form<HolidayOneDayForm> form = form(HolidayOneDayForm.class).bind(request().body().asJson());
		if (form.hasErrors()) {
			return badRequest(form.errorsAsJson());
		}

		final HolidayOneDayForm holidayOneDay = form.get();
		final DateTime date = holidayOneDay.date;
		final String trigramme = holidayOneDay.trigramme;
		final Mission mission = Mission.findById(holidayOneDay.holidayMissionId);
		final Cra cra = Cra.getOrCreate(trigramme, date.getYear(), date.getMonthOfYear());
		if (cra.isValidated) {
			return badRequest(ErrorMsgFactory.asJson("Impossible de poser des congés.", String.format("Le CRA de %s %s est validé.", cra.month.label, cra.year)));
		}
		final HolidayDTO dto = new HolidayDTO();
		dto.start = date;
		dto.end = date;
		dto.missionCode = mission.code;
		dto.nbDay = 0.0;
		dto.isValidated = true;
		dto.comment = (holidayOneDay.comment.isEmpty() ? null : holidayOneDay.comment);

		// Création de la journée de congé
		final Day day = new Day(date);
		//Si matin coché
		if (Boolean.TRUE.equals(holidayOneDay.morning)) {
			if (holidayOneDay.comment != null) {
				day.morning = new Holiday(mission, day.date, HalfDayType.MORNING, holidayOneDay.comment);
			} else {
				day.morning = new Holiday(mission, day.date, HalfDayType.MORNING);
			}

			dto.nbDay += 0.5;
			dto.halfDayType = HalfDayType.MORNING;
		}
		//Si après-midi coché
		if (Boolean.TRUE.equals(holidayOneDay.afternoon)) {
			if (holidayOneDay.comment != null && !Boolean.TRUE.equals(holidayOneDay.morning)) {
				day.afternoon = new Holiday(mission, day.date, HalfDayType.AFTERNOON, holidayOneDay.comment);
			} else {
				day.afternoon = new Holiday(mission, day.date, HalfDayType.AFTERNOON);
			}
			dto.nbDay += 0.5;
			dto.halfDayType = HalfDayType.AFTERNOON;
		}
		//Si les deux sont cochés
		if (Boolean.TRUE.equals(holidayOneDay.morning) && Boolean.TRUE.equals(holidayOneDay.afternoon)) {
			dto.halfDayType = null;
		}
		cra.createDays(Arrays.asList(day));

		// Send email
		async(Akka.future(new Callable<Result>() {
			@Override
			public Result call() throws Exception {
				Mailer.sendHolidayEmail(Employee.findByTrigramme(trigramme), mission, date, null, /**/
						dto.nbDay, dto.comment, holidayOneDay.morning, holidayOneDay.afternoon);
				return null;
			}
		}));
		return ok(toJson(dto));

	}

	/**
	 * Create holidayPeriod
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public static Result createHolidayPeriod() {
		Result result;
		final Form<HolidayPeriodForm> form = form(HolidayPeriodForm.class).bind(request().body().asJson());
		if (form.hasErrors()) {
			result = badRequest(form.errorsAsJson());
		} else {

			final HolidayPeriodForm holidayPeriod = form.get();
			final DateTime startHoliday = holidayPeriod.startHoliday;
			DateTime endHoliday = holidayPeriod.endHoliday;
			//Création de la liste des jours posés
			Map<DateTime, List<DateTime>> holidays = TimerHelper.getWorkingDaysInPeriod(startHoliday, endHoliday);
			final Mission mission = Mission.findById(holidayPeriod.holidayMissionId);
			List<Day> days = Lists.newArrayList();
			final HolidayDTO dto = new HolidayDTO();
			for (DateTime holiday : holidays.keySet()) {
				endHoliday = holidays.get(holiday).get(holidays.get(holiday).size() - 1);
				dto.nbDay = (double) holidays.get(holiday).size();
				dto.missionCode = mission.code;
				dto.comment = (holidayPeriod.comment.isEmpty() ? null : holidayPeriod.comment);
				if (Cra.getOrCreate(holidayPeriod.trigramme, holiday.getYear(), holiday.getMonthOfYear()).isLocked) {
					return badRequest(ErrorMsgFactory.asJson("La paye de " + TimerHelper.MonthType.getById(holiday.getMonthOfYear()).label /**/
							+ " a été générée.</br>Impossible de poser des congés."));
				} else {
					for (DateTime date : holidays.get(holiday)) {
						Day day = new Day(date);
						if (date.isEqual(startHoliday)) {
							if (Boolean.TRUE.equals(holidayPeriod.startMorning)) {
								if (holidayPeriod.comment != null) {
									day.morning = new Holiday(mission, day.date, HalfDayType.MORNING, holidayPeriod.comment);
								} else {
									day.morning = new Holiday(mission, day.date, HalfDayType.MORNING);
								}
							} else {
								dto.nbDay -= 0.5;
							}
							if (holidayPeriod.comment != null && !Boolean.TRUE.equals(holidayPeriod.startMorning)) {
								day.afternoon = new Holiday(mission, day.date, HalfDayType.AFTERNOON, holidayPeriod.comment);
							} else {
								day.afternoon = new Holiday(mission, day.date, HalfDayType.AFTERNOON);
							}
						} else if (date.isEqual(endHoliday)) {
							day.morning = new Holiday(mission, day.date, HalfDayType.MORNING);
							if (Boolean.TRUE.equals(holidayPeriod.endAfternoon)) {
								day.afternoon = new Holiday(mission, day.date, HalfDayType.AFTERNOON);
							} else {
								dto.nbDay -= 0.5;
							}
						} else {
							day.morning = new Holiday(mission, day.date, HalfDayType.MORNING);
							day.afternoon = new Holiday(mission, day.date, HalfDayType.AFTERNOON);
						}
						days.add(day);
					}
					if (!days.isEmpty()) {
						DateTime date = days.get(0).date;
						Cra.getOrCreate(holidayPeriod.trigramme, date.getYear(), date.getMonthOfYear())
								.createDays(days);
						dto.start = days.get(0).date;
						dto.end = days.get(days.size() - 1).date;
						dto.isValidated = true;

						days.clear();
					}
				}
			}
			result = ok(toJson(dto));
			async(Akka.future(new Callable<Result>() {
				@Override
				public Result call() throws Exception {
					Mailer.sendHolidayEmail(Employee.findByTrigramme(holidayPeriod.trigramme), mission, dto.start, dto.end,/**/
							dto.nbDay, dto.comment, holidayPeriod.startMorning, holidayPeriod.endAfternoon);
					return null;
				}
			}));
		}
		return result;
	}

	/**
	 * Create holidayRecurrent
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public static Result createHolidayRecurrent() {
		Result result;
		final Form<HolidayRecurrentForm> form = form(HolidayRecurrentForm.class).bind(request().body().asJson());
		if (form.hasErrors()) {
			result = badRequest(form.errorsAsJson());
		} else {
			final HolidayRecurrentForm holidayRecurrent = form.get();
			final Mission mission = Mission.findById(holidayRecurrent.holidayMissionId);
			DateTime startHoliday = new DateTime(holidayRecurrent.startHoliday);
			if (Cra.getOrCreate(holidayRecurrent.trigramme, startHoliday.getYear(), startHoliday.getMonthOfYear()).isLocked) {
				return badRequest(ErrorMsgFactory.asJson("La paye de " + TimerHelper.MonthType.getById(startHoliday.getMonthOfYear()).label /**/
						+ " a été générée.</br>Impossible de poser des congés."));
			} else {
				Double nbDays = 0.0;
				if (holidayRecurrent.endRecurrence.equals("after") && holidayRecurrent.nbOfOccurences != null) {
					//Récurrence se termine APRES nbOfOccurences occurences
					Integer occurences = 0,
							nbOfOccurrences = holidayRecurrent.nbOfOccurences * holidayRecurrent.daysOfWeek.size();
					while (!occurences.equals(nbOfOccurrences)) {
						//Si le jour n'est pas un Samedi,Dimanche ou jour Férié ET SI c'est un des jours de la semaine cochés
						if (holidayRecurrent.daysOfWeek.contains(startHoliday.getDayOfWeek())) {
							if (!TimerHelper.isDayOff(startHoliday)) {
								if (holidayRecurrent.comment != null) {
									saveDay(holidayRecurrent.trigramme, startHoliday, mission, /**/
											holidayRecurrent.morning, holidayRecurrent.afternoon, holidayRecurrent.comment);
								} else {
									saveDay(holidayRecurrent.trigramme, startHoliday, mission, /**/
											holidayRecurrent.morning, holidayRecurrent.afternoon, null);
								}
								if (Boolean.TRUE.equals(holidayRecurrent.morning) && Boolean.TRUE.equals(holidayRecurrent.afternoon)) {
									nbDays += 1;
								} else {
									nbDays += 0.5;
								}
							}
							occurences++;
						}
						//Lorsque l'on arrive à dimanche
						if (startHoliday.getDayOfWeek() == DateTimeConstants.SUNDAY) {
							//On ajoute X semaine si repaetEveryXWeeks est supérieur à 1
							startHoliday = (holidayRecurrent.repeatEveryXWeeks > 1) ? startHoliday.plusWeeks((holidayRecurrent.repeatEveryXWeeks - 1)) : startHoliday;
						}
						startHoliday = startHoliday.plusDays(1);
					}
				} else if (holidayRecurrent.endRecurrence.equals("at") && holidayRecurrent.endHoliday != null) {
					DateTime endHoliday = new DateTime(holidayRecurrent.endHoliday);
					if (Cra.getOrCreate(holidayRecurrent.trigramme, endHoliday.getYear(), endHoliday.getMonthOfYear()).isLocked) {
						return badRequest(ErrorMsgFactory.asJson("La paye de " + TimerHelper.MonthType.getById(endHoliday.getMonthOfYear()).label /**/
								+ " a été générée.</br>Impossible de poser des congés."));
					} else {
						//Récurrence se termine LE endHoliday
						while (!startHoliday.isAfter(endHoliday)) {
							//Si le jour n'est pas un Samedi,Dimanche ou jour Férié ET SI c'est un des jours de la semaine cochés
							if (holidayRecurrent.daysOfWeek.contains(startHoliday.getDayOfWeek()) && !TimerHelper.isDayOff(startHoliday)) {
								if (holidayRecurrent.comment != null) {
									saveDay(holidayRecurrent.trigramme, startHoliday, mission, /**/
											holidayRecurrent.morning, holidayRecurrent.afternoon, holidayRecurrent.comment);
								} else {
									saveDay(holidayRecurrent.trigramme, startHoliday, mission, /**/
											holidayRecurrent.morning, holidayRecurrent.afternoon, null);
								}
								if (Boolean.TRUE.equals(holidayRecurrent.morning) && Boolean.TRUE.equals(holidayRecurrent.afternoon)) {
									nbDays += 1;
								} else {
									nbDays += 0.5;
								}
							}
							//Lorsque l'on arrive à dimanche
							if (startHoliday.getDayOfWeek() == DateTimeConstants.SUNDAY) {
								//On ajoute X semaine si repeatEveryXWeeks est supérieur à 1
								startHoliday = (holidayRecurrent.repeatEveryXWeeks > 1) ? startHoliday.plusWeeks((holidayRecurrent.repeatEveryXWeeks - 1)) : startHoliday;
							}
							startHoliday = startHoliday.plusDays(1);
						}
					}

				}
				result = ok();
				final Double finalNbDays = nbDays;
				async(Akka.future(new Callable<Result>() {
					@Override
					public Result call() throws Exception {
						String comment = holidayRecurrent.comment.isEmpty() ? null : holidayRecurrent.comment;
						Mailer.sendHolidayRecurrentEmail(Employee.findByTrigramme(holidayRecurrent.trigramme), mission, /**/
								holidayRecurrent.startHoliday, holidayRecurrent.endHoliday, holidayRecurrent.repeatEveryXWeeks, /**/
								holidayRecurrent.daysOfWeek, holidayRecurrent.nbOfOccurences, holidayRecurrent.morning, /**/
								holidayRecurrent.afternoon, comment, finalNbDays);

						return null;
					}
				}));
			}
		}
		return result;
	}

	@BodyParser.Of(BodyParser.Json.class)
	public static Result holidaysExisteOnDates() {
		final Form<CheckHolidayForm> form = form(CheckHolidayForm.class).bind(request().body().asJson());
		if (form.hasErrors()) {
			return badRequest(form.errorsAsJson());
		}
		CheckHolidayForm chForm = form.get();
		final String trigramme = chForm.trigramme;

		final DateTime start;
		final DateTime end;
		final Boolean morning;
		final Boolean afternoon;
		if (chForm.date != null) {
			start = chForm.date;
			end = chForm.date;
			morning = chForm.morning;
			afternoon = chForm.afternoon;
		} else {
			start = chForm.startHoliday;
			end = chForm.endHoliday;
			morning = chForm.startMorning;
			afternoon = chForm.endAfternoon;
		}

		final List<Day> days = Cra.findDaysByTrigrammeBetweenDates(trigramme, start, end, morning, afternoon);
		return ok(toJson(!days.isEmpty()));

	}

	/**
	 * getAll
	 */
	public static Result fetchHolidays(final String trigramme, final Integer year) throws InterruptedException {
		HolidayDataTableDTO dataTableDTO = new HolidayDataTableDTO();
		List<Holiday> holidays = Cra.getHolidays(trigramme, year, Boolean.FALSE);

		List<List<Holiday>> createdHolidays = Lists.newArrayList();
		Holiday ref = null;
		List<Holiday> c = null;
		for (Holiday holiday : holidays) {
			if (ref == null) {
				ref = holiday;
				c = Lists.newArrayList(holiday);
			} else if (TimerHelper.follow(ref, holiday)) {
				c.add(holiday);
				ref = holiday;
			} else {
				createdHolidays.add(c);
				c = Lists.newArrayList(holiday);
				ref = holiday;
			}
		}
		if (c != null && !c.isEmpty()) {
			createdHolidays.add(c);
		}
		for (List<Holiday> holiday : createdHolidays) {
			HolidayDTO holidayDTO = new HolidayDTO();
			holidayDTO.missionCode = holiday.get(0).mission.code;
			holidayDTO.start = holiday.get(0).date;
			holidayDTO.end = holiday.get(holiday.size() - 1).date;
			if (holiday.get(0).comment != null) {
				holidayDTO.comment = holiday.get(0).comment;
			}
			holidayDTO.nbDay = new BigDecimal(holiday.size()).divide(new BigDecimal(2)).doubleValue();
			if (holidayDTO.nbDay < 1) {
				holidayDTO.halfDayType = holiday.get(0).halfDayType;
			} else if ((holiday.size() % 2) != 0) {
				if (HalfDayType.AFTERNOON.equals(holiday.get(0).halfDayType)) {
					holidayDTO.halfDayType = HalfDayType.AFTERNOON;
				} else {
					holidayDTO.halfDayType = HalfDayType.MORNING;
				}
			}
			holidayDTO.isValidated = holiday.get(0).isValidated;
			dataTableDTO.holidays.add(holidayDTO);
		}
		final JsonNode json = toJson(dataTableDTO);
		return ok(json);

	}

	/**
	 * DeleteHolidays
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public static Result deleteHoliday() {
		Result result;
		final Form<HolidayDeleteForm> createForm = form(HolidayDeleteForm.class).bind(request().body().asJson());
		if (createForm.hasErrors()) {
			result = badRequest(createForm.errorsAsJson());
		} else {
			String trigramme = createForm.get().trigramme;
			DateTime start = createForm.get().startHoliday;
			DateTime end = createForm.get().endHoliday;

			Cra.deleteHolidays(trigramme, start, end);

			result = ok();
		}
		return result;
	}

	/**
	 * Sauvegarde la journée
	 *
	 * @param trigramme
	 * @param date
	 * @param mission
	 * @param morning
	 * @param afternoon
	 * @param comment
	 * @return la journée sauvegardée
	 */
	private static Day saveDay(String trigramme, DateTime date, Mission mission, Boolean morning, Boolean afternoon, String comment) {
		Day day = new Day(date);
		//Si matin coché
		if (Boolean.TRUE.equals(morning)) {
			day.morning = new Holiday(mission, day.date, HalfDayType.MORNING, comment);
		}
		//Si après-midi coché
		if (Boolean.TRUE.equals(afternoon)) {
			if (!Boolean.TRUE.equals(morning)) {
				day.afternoon = new Holiday(mission, day.date, HalfDayType.AFTERNOON, comment);
			} else {
				day.afternoon = new Holiday(mission, day.date, HalfDayType.AFTERNOON);
			}
		}

		Cra.getOrCreate(trigramme, day.date.getYear(), day.date.getMonthOfYear())
				.createDays(Arrays.asList(day));
		return day;
	}
}
