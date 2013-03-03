package helpers.time;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import constants.HalfDayType;
import dto.holiday.RecurrentDayDTO;
import models.Holiday;
import models.Month;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.joda.time.LocalTime;
import play.libs.F;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author f.patin
 *         Date: 22/07/12
 *         Time: 13:30
 */

public class TimerHelper {

	public static final LocalTime START_AURORA = new LocalTime(0, 0, 0);
	public static final LocalTime START_DAY = new LocalTime(6, 0, 0);
	public static final LocalTime START_DUSK = new LocalTime(21, 0, 0);

	public static final Duration HALF_DAY_DURATION = new Duration(3 * 60 * 60 * 1000 + 42 * 60 * 1000);
	public static final Duration DAY_DURATION = HALF_DAY_DURATION.plus(HALF_DAY_DURATION);
	public static final Duration WEEK_DURATION = new Duration(37 * 60 * 60 * 1000);

	public static final BigDecimal GENESIS_HALF_DAY = new BigDecimal(0.5D);



	public static enum ExtraTimeType {
		AT_125(new Duration(37 * 60 * 60 * 1000), new Duration(45 * 60 * 60 * 1000)),
		AT_150(new Duration(45 * 60 * 60 * 1000), null);

		public final Duration min;
		public final Duration max;

		ExtraTimeType(final Duration min, final Duration max) {
			this.min = min;
			this.max = max;
		}
	}

	public static enum MonthType {
		JANUARY(DateTimeConstants.JANUARY, "Janvier"),
		FEBRUARY(DateTimeConstants.FEBRUARY, "Février"),
		MARCH(DateTimeConstants.MARCH, "Mars"),
		APRIL(DateTimeConstants.APRIL, "Avril"),
		MAY(DateTimeConstants.MAY, "Mai"),
		JUNE(DateTimeConstants.JUNE, "Juin"),
		JULY(DateTimeConstants.JULY, "Juillet"),
		AUGUST(DateTimeConstants.AUGUST, "Août"),
		SEPTEMBER(DateTimeConstants.SEPTEMBER, "Septembre"),
		OCTOBER(DateTimeConstants.OCTOBER, "Octobre"),
		NOVEMBER(DateTimeConstants.NOVEMBER, "Novembre"),
		DECEMBER(DateTimeConstants.DECEMBER, "Décembre");

		public final Integer id;
		public final String label;

		private MonthType(final Integer id, final String label) {
			this.id = id;
			this.label = label;
		}

		public static MonthType getById(Integer id) {
			checkArgument(id != null, "id must be not null");
			for (MonthType month : MonthType.values()) {
				if (month.id.equals(id)) {
					return month;
				}
			}
			throw new IllegalArgumentException(String.format("The id[%s] does not exist", id));
		}

		public static Month toMonth(Integer id) {
			MonthType month = getById(id);
			return new Month(month);
		}

		public static List<Month> all() {
			List<Month> result = Lists.transform(Arrays.asList(MonthType.values()), new Function<MonthType, Month>() {
				@Override
				public Month apply(@Nullable MonthType monthType) {
					return new Month(monthType);
				}
			});
			return result;
		}

	}

	private DaysOff daysOff = new DaysOff();
	private Integer year;
	private MonthType monthType;

	public TimerHelper(Integer year, MonthType monthType) {
		this.year = year;
		this.monthType = monthType;
	}

	public TimerHelper(Integer year, Integer month) {
		this.year = year;
		this.monthType = MonthType.getById(month);
	}

	public TimerHelper(Integer year) {
		this.year = year;
		this.monthType = MonthType.JANUARY;
	}

	public TimerHelper(DateTime dt) {
		this.year = dt.getYear();
		this.monthType = MonthType.getById(dt.getMonthOfYear());
	}

	public TimerHelper() {
	}

	public static boolean follow(Holiday start, Holiday end) {
		if (!start.mission.code.equals(end.mission.code)) {
			return false;
		}
		DateTime dtStart = start.date.withTimeAtStartOfDay();
		DateTime dtEnd = end.date.withTimeAtStartOfDay();
		Duration d = new Duration(dtStart, dtEnd);
		if (HalfDayType.MORNING.equals(start.halfDayType) && HalfDayType.AFTERNOON.equals(end.halfDayType) && dtStart.isEqual(dtEnd)) {
			return true;
		} else if (HalfDayType.AFTERNOON.equals(start.halfDayType) && HalfDayType.MORNING.equals(end.halfDayType)) {
			int ecart = 1;
			if (DateTimeConstants.FRIDAY == dtStart.getDayOfWeek() && DateTimeConstants.MONDAY == dtEnd.getDayOfWeek()) {
				ecart = 3;
			}
			if (TimerHelper.isDayOff(dtEnd.minusDays(1)) && !TimerHelper.isSaturdayOrSunday(dtEnd.minusDays(1))) {
				ecart = 2;
			}
			if (d.getStandardDays() == ecart) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public boolean isInMonth(DateTime date) {
		return this.monthType.id.equals(date.getMonthOfYear()) && this.year.equals(date.getYear());
	}

	public Integer getNbTheoreticalWorkingDays() {
		DateTime firstDay = new DateTime(this.year, this.monthType.id, 1, 0, 0);
		Integer lastDayOfTheMonth = firstDay.dayOfMonth().withMaximumValue().getDayOfMonth();
		Integer nbWorkingDays = 0;
		for (int i = 0; i < lastDayOfTheMonth; i++) {
			DateTime dt = firstDay.plusDays(i);
			nbWorkingDays = (DaysOff.isSaturdayOrSunday(dt) || DaysOff.isDayOff(dt)) ? nbWorkingDays : nbWorkingDays + 1;
		}
		return nbWorkingDays;
	}

	public static Integer getNbTheoreticalWorkingDays(final Integer year, final Integer month) {
		DateTime firstDay = new DateTime(year, month, 1, 0, 0);
		Integer lastDayOfTheMonth = firstDay.dayOfMonth().withMaximumValue().getDayOfMonth();
		Integer nbWorkingDays = 0;
		for (int i = 0; i < lastDayOfTheMonth; i++) {
			DateTime dt = firstDay.plusDays(i);
			nbWorkingDays = (DaysOff.isSaturdayOrSunday(dt) || DaysOff.isDayOff(dt)) ? nbWorkingDays : nbWorkingDays + 1;
		}
		return nbWorkingDays;
	}

	public RecurrentDayDTO getNbOfWorkingDaysInPeriod(DateTime startPeriod, DateTime endPeriod, /**/
	                                                  Boolean startMorning, Boolean startAfternoon, Boolean endAfternoon) {
		Double nbOfWorkingDays = 0.0;
		DateTime start = new DateTime(startPeriod);
		DateTime end = new DateTime(endPeriod);
		Boolean dayOffFind = Boolean.FALSE;
		RecurrentDayDTO dto = new RecurrentDayDTO();
		while (!start.isAfter(end)) {
			//Si ce n'est pas un Samedi, ni un Dimanche, ni un jour férié
			if (!(this.daysOff.isDayOff(start)) /**/ && /**/ !(this.daysOff.isSaturdayOrSunday(start))) {
				nbOfWorkingDays++;
			}
			if (this.daysOff.isDayOff(start)) {
				dayOffFind = Boolean.TRUE;
			}
			start = start.plusDays(1);
		}
		//S'il n'y a qu'un jour
		if (nbOfWorkingDays == 1) {
			if (!startMorning) {
				nbOfWorkingDays -= 0.5;
			}
			if (!startAfternoon) {
				nbOfWorkingDays -= 0.5;
			}
		} else {
			if (!startMorning) {
				nbOfWorkingDays -= 0.5;
			}
			if (!endAfternoon) {
				nbOfWorkingDays -= 0.5;
			}
		}
		dto.findDayOff = dayOffFind;
		dto.nbDay = nbOfWorkingDays;

		return dto;
	}

	public RecurrentDayDTO getNbOfWorkingRecurrentDays(DateTime startRecurrence, Integer repeatEveryXWeeks, /**/
	                                                   Integer nbOfOccurences, DateTime endRecurrence, List<Integer> daysOfWeek, /**/
	                                                   Boolean morning, Boolean afternoon) {
		Double nbOfWorkingDays = 0.0;
		DateTime start = new DateTime(startRecurrence);
		DateTime lastDay = new DateTime();
		Boolean dayOffFind = Boolean.FALSE;
		RecurrentDayDTO dto = new RecurrentDayDTO();
		if (nbOfOccurences != null) {
			Integer tmp = 0;
			nbOfOccurences *= daysOfWeek.size();
			while (nbOfOccurences != tmp) {
				//Si le jour n'est pas un Samedi,Dimanche ou jour Férié ET SI c'est un des jours de la semaine cochés
				if (daysOfWeek.contains(start.getDayOfWeek())) {
					if (this.daysOff.isDayOff(start)) {
						dayOffFind = Boolean.TRUE;
					} else {
						nbOfWorkingDays++;
						lastDay = new DateTime(start);
						if (nbOfWorkingDays == 1) {
							dto.start = new DateTime(start);
						}
					}
					tmp++;
				}
				//Lorsque l'on arrive à dimanche
				if (start.getDayOfWeek() == DateTimeConstants.SUNDAY) {
					//On ajoute X semaine si repaetEveryXWeeks est supérieur à 1
					start = (repeatEveryXWeeks > 1) ? start.plusWeeks((repeatEveryXWeeks - 1)) : start;
				}
				start = start.plusDays(1);
			}
		} else if (endRecurrence != null) {
			DateTime end = new DateTime(endRecurrence);
			while (!start.isAfter(end)) {
				//Si ce n'est pas un Samedi, ni un Dimanche, ni un jour férié
				if (daysOfWeek.contains(start.getDayOfWeek())) {
					if (this.daysOff.isDayOff(start)) {
						dayOffFind = Boolean.TRUE;
					} else {
						nbOfWorkingDays++;
						lastDay = new DateTime(start);
						if (nbOfWorkingDays == 1) {
							dto.start = new DateTime(start);
						}
					}
				}
				//Lorsque l'on arrive à dimanche
				if (start.getDayOfWeek() == DateTimeConstants.SUNDAY) {
					//On ajoute X semaine si repaetEveryXWeeks est supérieur à 1
					start = (repeatEveryXWeeks > 1) ? start.plusWeeks((repeatEveryXWeeks - 1)) : start;
				}
				start = start.plusDays(1);
			}
		}
		dto.end = lastDay;
		if (morning && afternoon) {
			dto.nbDay = nbOfWorkingDays;
		} else {
			dto.nbDay = nbOfWorkingDays / 2;
		}
		dto.findDayOff = dayOffFind;
		dto.isOnlyDay = dto.start.equals(dto.end) ? true : false;

		return dto;
	}

	/**
	 * get all working days (excluding Saturday, sunday and DayOff) between 2 dates
	 *
	 * @param startPeriod
	 * @param endPeriod
	 * @return a Map which contains for each 1st day of month/year, the list of working day in month/year
	 */
	public static Map<DateTime, List<DateTime>> getWorkingDaysInPeriod(DateTime startPeriod, DateTime endPeriod) {
		Map<DateTime, List<DateTime>> workingDays = Maps.newHashMap();
		Integer year = startPeriod.getYear();
		Integer month = startPeriod.getMonthOfYear();
		workingDays.put(startPeriod.withDayOfMonth(1), new ArrayList<DateTime>());
		while (!startPeriod.isAfter(endPeriod)) {
			//Si ce n'est pas un Samedi, ni un Dimanche, ni un jour férié
			if (!(DaysOff.isDayOff(startPeriod)) && !(DaysOff.isSaturdayOrSunday(startPeriod))) {
				DateTime firstDayOfMonthAndYear = startPeriod.withDayOfMonth(1);
				if (!year.equals(startPeriod.getYear()) || !month.equals(startPeriod.getMonthOfYear())) {
					year = startPeriod.getYear();
					month = startPeriod.getMonthOfYear();
					workingDays.put(firstDayOfMonthAndYear, new ArrayList<DateTime>());
				}
				workingDays.get(firstDayOfMonthAndYear).add(startPeriod);
			}
			startPeriod = startPeriod.plusDays(1);
		}
		return workingDays;
	}

	public static Boolean containsOnlyDayOffInPeriod(DateTime start, DateTime end) {
		if (start.isEqual(end) && !(DaysOff.isDayOff(start)) && !(DaysOff.isSaturdayOrSunday(start))) {
			return Boolean.FALSE;
		}
		while (!start.isAfter(end)) {
			if (!(DaysOff.isDayOff(start)) && !(DaysOff.isSaturdayOrSunday(start))) {
				return Boolean.FALSE;
			}
			start = start.plusDays(1);
		}
		return Boolean.TRUE;
	}

	public F.Tuple<DateTime, DateTime> getStartEndWeek(Integer week) {
		DateTime dt = new DateTime().withYear(this.year).withWeekOfWeekyear(week).withTime(0, 0, 0, 0);
		return F.Tuple(dt.withDayOfWeek(DateTimeConstants.MONDAY), dt.withDayOfWeek(DateTimeConstants.SUNDAY));
	}

	/**
	 * Return a List of <code>DateTime</code> corresponding to a <code>month</code>
	 * TODO changer en ImmutableSortedSet
	 *
	 * @return
	 */
	public List<DateTime> getMonthDays() {
		DateTime dt = new DateTime(this.year, this.monthType.id, 1, 0, 0);
		Integer lastDayOfTheMonth = dt.dayOfMonth().withMaximumValue().getDayOfMonth();
		List<DateTime> monthDays = Lists.newArrayListWithCapacity(lastDayOfTheMonth);
		for (int i = 1; i <= lastDayOfTheMonth; i++) {
			monthDays.add(new DateTime(this.year, this.monthType.id, i, 0, 0));
		}
		return monthDays;
	}

	public DateTime getFirstDayOfMonth() {
		return new DateTime(this.year, this.monthType.id, 1, 0, 0);
	}

	public static DateTime getFirstDayOfMonth(final Integer year, final Integer month) {
		return new DateTime(year, month, 1, 0, 0);
	}

	public static DateTime getFirstDayOfWeek(final Integer year, final Integer week) {
		return new DateTime(year, 1,1,0,0).withWeekOfWeekyear(week).withDayOfWeek(DateTimeConstants.MONDAY);
	}

	public static DateTime getLastDayOfMonth(final Integer year, final Integer month) {
		return getFirstDayOfMonth(year, month).dayOfMonth().withMaximumValue();
	}

	public static List<DateTime> getDaysOfMonthByDayOfWeek(Integer year, Integer month, List<Integer> daysOfWeek, final Boolean withoutDayOff) {
		final DateTime firstDayOfMonth = getFirstDayOfMonth(year, month);
		final DateTime endDayOfWeek = getLastDayOfMonth(year, month);
		DateTime currentDay = firstDayOfMonth;
		final List<DateTime> result = Lists.newArrayList();
		while (!currentDay.isAfter(endDayOfWeek)) {
			if (daysOfWeek.contains(currentDay.dayOfWeek().get())) {
				if (Boolean.TRUE.equals(withoutDayOff)) {
					if (!TimerHelper.isDayOff(currentDay)) {
						result.add(currentDay);
					}
				} else {
					result.add(currentDay);
				}
			}
			currentDay = currentDay.plusDays(1);
		}
		return result;
	}

	public List<DateTime> completeToCalendar(List<DateTime> calendar) {
		List<DateTime> result = Lists.newArrayList(calendar);
		DateTime firstDayOfMonth = new DateTime(this.year, this.monthType.id, 1, 0, 0);
		DateTime firstDayOfCalendar = getMondayOfDate(firstDayOfMonth);
		long nbDays = new Duration(firstDayOfCalendar, firstDayOfMonth).getStandardDays();
		for (int i = 0; i < nbDays; i++) {
			result.add(firstDayOfCalendar.plusDays(i));
		}
		Collections.sort(result);
		DateTime lastDayOfMonth = result.get(result.size() - 1);
		DateTime lastDayOfCalendar = getSundayOfWeek(lastDayOfMonth);
		nbDays = new Duration(lastDayOfMonth, lastDayOfCalendar).getStandardDays();
		for (int i = 1; i <= nbDays; i++) {
			result.add(lastDayOfMonth.plusDays(i));
		}
		return result;
	}

	public List<DateTime> getWeekDays(DateTime monday) {
		List<DateTime> weekDays = Lists.newArrayListWithCapacity(DateTimeConstants.DAYS_PER_WEEK);
		for (int day = DateTimeConstants.MONDAY; day <= DateTimeConstants.SUNDAY; day++) {
			weekDays.add(monday.plusDays(day - 1));
		}
		return weekDays;
	}

	public static DateTime getMondayOfDate(DateTime firstDay) {
		return firstDay.minusDays(firstDay.getDayOfWeek() - DateTimeConstants.MONDAY);
	}

	public DateTime getSundayOfWeek(DateTime lastDay) {
		return lastDay.plusDays(DateTimeConstants.SUNDAY - lastDay.getDayOfWeek());
	}

	public Integer getNbDaysOff() {
		DateTime firstDay = new DateTime(this.year, this.monthType.id, 1, 0, 0);
		Integer lastDayOfTheMonth = firstDay.dayOfMonth().withMaximumValue().getDayOfMonth();
		Integer nbDaysOff = 0;

		for (int i = 0; i < lastDayOfTheMonth; i++) {
			DateTime dt = firstDay.plusDays(i);
			nbDaysOff = (this.daysOff.isDayOff(dt)) ? nbDaysOff + 1 : nbDaysOff;
		}

		return nbDaysOff;
	}

	public Month toMonth() {
		return MonthType.toMonth(this.monthType.id);
	}

	public Boolean isDayOff(Integer day) {
		return DaysOff.isDayOff(new DateTime(this.year, this.monthType.id, day, 0, 0));
	}

	public static Boolean isDayOff(DateTime day) {
		return DaysOff.isDayOff(day);
	}

	public static boolean isSaturdayOrSunday(DateTime date) {
		return DaysOff.isSaturdayOrSunday(date);
	}

	public static boolean isSaturday(DateTime date) {
		return DaysOff.isSaturday(date);
	}

	public static boolean isSunday(DateTime date) {
		return DaysOff.isSunday(date);
	}

	public Boolean isInPastOrFuture(DateTime date) {
		return (this.monthType.id != date.getMonthOfYear()) || (this.year != date.getYear());
	}

	public static DateTime getEaster(Integer year) {
		return DaysOff.getEaster(year);
	}

	public static BigDecimal toGenesisDay(final LocalTime start, LocalTime end) {
		return toGenesisDay(new Duration(start.toDateTimeToday(), end.toDateTimeToday()));
	}

	public static BigDecimal toGenesisDay(final Duration duration) {
		return new BigDecimal(duration.getMillis()) /**/
				.divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP) /**/
				.divide(new BigDecimal(60), 2, RoundingMode.HALF_UP) /**/
				.divide(new BigDecimal(60), 2, RoundingMode.HALF_UP) /**/
				.divide(new BigDecimal(7.4), 2, RoundingMode.HALF_UP);
	}

	public static BigDecimal toRealDay(final LocalTime start, LocalTime end) {
		return toRealDay(new Duration(start.toDateTimeToday(), end.toDateTimeToday()));
	}

	public static BigDecimal toRealDay(final Duration duration) {
		return new BigDecimal(duration.getMillis()) /**/
				.divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP) /**/
				.divide(new BigDecimal(60), 2, RoundingMode.HALF_UP) /**/
				.divide(new BigDecimal(60), 2, RoundingMode.HALF_UP) /**/
				.divide(new BigDecimal(24), 2, RoundingMode.HALF_UP);
	}

	public static List<Integer> getWeeksNumber(final Integer year, final Integer month, final Boolean extended) {
		return Boolean.TRUE.equals(extended) ? getExtendedWeeksNumber(year, month) : getWeeksNumber(year, month);
	}

	private static List<Integer> getWeeksNumber(final Integer year, final Integer month){
		List<Integer> weeksNumber = Lists.newArrayList();
		boolean finished = false;
		int i = 0;
		DateTime firstDay = TimerHelper.getMondayOfDate(new DateTime(year, month, 1, 0, 0));
		while (!finished) {
			DateTime monday = firstDay.plusWeeks(i);
			DateTime previousSunday = monday.minusDays(1);
			DateTime nextSunday = monday.plusDays(6);
			if (previousSunday.getMonthOfYear() <= month && nextSunday.getMonthOfYear() == month) {
				weeksNumber.add(monday.getWeekOfWeekyear());
				i++;
			} else {
				finished = true;
			}
		}
		return weeksNumber;
	}
	private static List<Integer> getExtendedWeeksNumber(final Integer year, final Integer month){
		List<Integer> weeksNumber = Lists.newArrayList();
		final DateTime firstDay = TimerHelper.getMondayOfDate(new DateTime(year, month, 1, 0, 0));
		weeksNumber.add(firstDay.getWeekOfWeekyear());
		DateTime monday = firstDay.plusDays(7);
		while(monday.getMonthOfYear() == month){
			weeksNumber.add(monday.getWeekOfWeekyear());
			monday = monday.plusDays(7);
		}
		return weeksNumber;
	}

}
