package controllers;

import be.objectify.deadbolt.actions.And;
import be.objectify.deadbolt.actions.Restrictions;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import constants.Pattern;
import dto.holiday.RecurrentDayDTO;
import helpers.deserializer.DateTimeDeserializer;
import helpers.formatter.JodaDateTimeFormatter;
import helpers.time.TimerHelper;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.joda.time.DateTime;
import play.data.Form;
import play.mvc.Result;
import play.mvc.Security;
import security.RoleName;
import security.Secured;

import java.util.List;

import static play.libs.Json.toJson;

/**
 * User: LPU
 * Date: 17/09/12
 * Time: 15:42
 */
@Security.Authenticated(Secured.class)
@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
public class Times extends AbstractController {

	public static class ComputeForm {
		@JsonDeserialize(using = DateTimeDeserializer.class)
		@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
		public DateTime startHoliday;

		@JsonDeserialize(using = DateTimeDeserializer.class)
		@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
		public DateTime endHoliday;

		public Boolean startMorning;

		public Boolean startAfternoon;

		public Boolean endMorning;

		public Boolean endAfternoon;

		public String validate() {
			if (endHoliday != null) {
				return (startHoliday.isAfter(endHoliday) ? "La date de début doit être avant la date de fin !" : null);
			}
			return null;
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(this)/**/
					.add("startHoliday", startHoliday)/**/
					.add("endHoliday", endHoliday)/**/
					.add("startMorning", startMorning)/**/
					.add("startAfternoon", startAfternoon)/**/
					.add("endMorning", endMorning)/**/
					.add("endAfternoon", endAfternoon)/**/
					.toString();
		}
	}

	public static class ComputeRecurrentForm {
		@JsonDeserialize(using = DateTimeDeserializer.class)
		@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
		public DateTime startRecurrence;
		@JsonProperty
		public Integer repeatEveryXWeeks;
		@JsonProperty
		public Integer nbOfOccurences;
		@JsonDeserialize(using = DateTimeDeserializer.class)
		@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
		public DateTime endRecurrence;

		public Boolean morning;
		public Boolean afternoon;

		@JsonProperty
		public List<Integer> daysOfWeek = Lists.newArrayList();

		public String validate() {
			if (endRecurrence != null) {
				return (startRecurrence.isAfter(endRecurrence) ? "La date de début doit être avant la date de fin !" : null);
			}
			return null;
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(this)/**/
					.add("startRecurrence", startRecurrence)/**/
					.add("nbOfOccurences", nbOfOccurences)/**/
					.add("repeatEveryXWeeks", repeatEveryXWeeks)/**/
					.add("endRecurrence", endRecurrence)/**/
					.add("daysOfWeek", daysOfWeek)/**/
					.toString();
		}
	}

	public static Result computeWorkingDays() {
		Result result = null;
		final Form<ComputeForm> computeForm = form(ComputeForm.class).bind(request().body().asJson());
		if (computeForm.hasErrors()) {
			result = badRequest(computeForm.errorsAsJson());
		} else {
			DateTime startPeriod = computeForm.get().startHoliday;
			DateTime endPeriod = computeForm.get().endHoliday;
			Boolean startMorning = computeForm.get().startMorning == null ? true : computeForm.get().startMorning;
			Boolean startAfternoon = computeForm.get().startAfternoon == null ? true : computeForm.get().startAfternoon;
			Boolean endAfternoon = computeForm.get().endAfternoon == null ? true : computeForm.get().endAfternoon;

			TimerHelper timer = new TimerHelper(startPeriod.getYear(), TimerHelper.MonthType.getById(startPeriod.getMonthOfYear()));
			RecurrentDayDTO dto = timer.getNbOfWorkingDaysInPeriod(startPeriod, endPeriod, startMorning, startAfternoon, endAfternoon);

			result = ok(toJson(dto));
		}
		return result;
	}

	public static Result computeWorkingRecurrentDays() {
		Result result = null;
		final Form<ComputeRecurrentForm> computeForm = form(ComputeRecurrentForm.class).bind(request().body().asJson());
		if (computeForm.hasErrors()) {
			result = badRequest(computeForm.errorsAsJson());
		} else {
			DateTime startRecurrence = computeForm.get().startRecurrence;
			Integer nbOfOccurences = computeForm.get().nbOfOccurences;
			DateTime endRecurrence = computeForm.get().endRecurrence;
			Integer repeatEveryXWeeks = computeForm.get().repeatEveryXWeeks;

			List<Integer> daysOfWeek = computeForm.get().daysOfWeek;

			TimerHelper timer = new TimerHelper(startRecurrence.getYear(), TimerHelper.MonthType.getById(startRecurrence.getMonthOfYear()));
			RecurrentDayDTO dto = timer.getNbOfWorkingRecurrentDays(startRecurrence, repeatEveryXWeeks, /**/
					nbOfOccurences, endRecurrence, daysOfWeek, /**/
					computeForm.get().morning, computeForm.get().afternoon);

			result = ok(toJson(dto));
		}
		return result;
	}

	public static Result isWorkingDay() {
		Result result = null;
		final Form<ComputeForm> computeForm = form(ComputeForm.class).bind(request().body().asJson());
		if (computeForm.hasErrors()) {
			result = badRequest(computeForm.errorsAsJson());
		} else {
			DateTime startPeriod = computeForm.get().startHoliday;
			if (TimerHelper.isDayOff(startPeriod) || TimerHelper.isSaturdayOrSunday(startPeriod)) {
				result = ok(toJson(false));
			} else {
				result = ok(toJson(true));
			}
		}
		return result;
	}

	public static Result getNbOpenedDays(Integer year, Integer month) {
		return ok(toJson(new TimerHelper(year, TimerHelper.MonthType.getById(month)).getNbTheoreticalWorkingDays()));
	}
}
