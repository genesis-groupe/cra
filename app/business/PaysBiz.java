package business;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import constants.MissionType;
import helpers.exception.CraNotFoundException;
import helpers.exception.PayIllegalStateException;
import helpers.time.DaysOff;
import helpers.time.TimerHelper;
import models.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.Duration;
import play.libs.F;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author : f.patin
 *         Date: 05/11/12
 *         Time: 12:40
 */

public class PaysBiz {

	public static Pay getOrCompute(final String trigramme, final Integer year, final Integer month, final BigDecimal hourlyRate) throws CraNotFoundException {
		Pay pay = Pay.find(trigramme, year, month);
		if (pay == null || !Boolean.TRUE.equals(pay.isValidated)) {
			Cra cra = Cra.findByTrigrammeAndYearAndMonth(trigramme, year, month);
			if (cra == null) {
				throw new CraNotFoundException(String.format("Collaborateur : %s, Cra : %s/%s, Il n'y a pas de Cra saisie pour ce collaborateur", trigramme, TimerHelper.MonthType.toMonth(month).label, year));
			}

			Pay.safeDelete(trigramme, year, month);
			pay = extractTimes(trigramme, year, month);
			pay.idCra = cra.id;
			pay.isCraValidated = cra.isValidated;
			pay.craComment = cra.comment;
			pay.monthDayCounter = cra.computeMonthDayCounter(new TimerHelper(year, month));
			pay.missionCounters.addAll(cra.computeMissionCounter(MissionType.CUSTOMER));
			pay.standBys.putAll(cra.standBys);
			pay.fees.addAll(cra.fees);
			pay.insert();
		}
		pay.computeAmountCounter(hourlyRate);
		return pay;
	}

	public static Pay getForExport(String trigramme, Integer year, Integer month, BigDecimal hourlyRate) throws PayIllegalStateException {
		Pay pay = Pay.find(trigramme, year, month);
		if (pay == null) {
			throw new PayIllegalStateException(String.format("La rémunération de la période %s/%s pour le collaborateur %s n'existe pas", month, year, trigramme));
		}
		pay.computeAmountCounter(hourlyRate);
		return pay;
	}

	private static Pay extractTimes(final String trigramme, final Integer year, final Integer month) {
		Duration monthTotalTimes = Duration.ZERO;
		Duration monthNormalTimes = Duration.ZERO;
		Duration monthExtraTimes125 = Duration.ZERO;
		Duration monthExtraTimes150 = Duration.ZERO;
		Duration monthNightlyTimes = Duration.ZERO;
		Duration monthSundayTimes = Duration.ZERO;
		Duration monthDayOffTimes = Duration.ZERO;

		Map<DateTime, Set<Day>> map = findDaysByWeeks(trigramme, year, month);

		Pay pay = new Pay(Employee.findByTrigramme(trigramme), year, month);

		Set<WeekCounter> weeksCounter = Sets.newTreeSet(WeekCounter.BY_START_DATE);
		// Compute Weeks duration
		for (DateTime firstDayOfWeek : map.keySet()) {
			final Set<Day> weekDays = map.get(firstDayOfWeek);

			Duration dailyHours = Duration.ZERO;
			Duration nightlyHours = Duration.ZERO;
			Duration sundayTime = Duration.ZERO;
			final List<DayTimeCounter> daysTimeCounters = Lists.newArrayListWithExpectedSize(5);

			for (Day day : weekDays) {
				F.Tuple3<Duration, Duration, Duration> t = day.computeDurations();
				final Duration dayTotalTimes = Duration.ZERO.plus(t._1).plus(t._2);
				final DayTimeCounter dayTimeCounter = new DayTimeCounter();
				dayTimeCounter.date = day.date;
				dayTimeCounter.totalTimes = new TimeDuration(dayTotalTimes);
				dayTimeCounter.normalTimes = new TimeDuration(computeNormalTimesByDay(dayTotalTimes));
				//dayTimeCounter.extraTimes125 = new TimeDuration(computeExtraTimes(dayTotalTimes, TimerHelper.ExtraTimeType.AT_125));
				//dayTimeCounter.extraTimes150 = new TimeDuration(computeExtraTimes(dayTotalTimes, TimerHelper.ExtraTimeType.AT_150));
				dayTimeCounter.nightlyTimes = new TimeDuration(t._2);
				dayTimeCounter.sundayTimes = new TimeDuration(t._3);
				dayTimeCounter.dayOffTimes = new TimeDuration(computeDayOffTimes(day.date, t));
				daysTimeCounters.add(dayTimeCounter);

				dailyHours = dailyHours.plus(t._1);
				nightlyHours = nightlyHours.plus(t._2);
				sundayTime = sundayTime.plus(t._3);
			}

			Duration totalTimes = dailyHours.plus(nightlyHours);
			WeekCounter weekCounter = new WeekCounter(firstDayOfWeek);
			weekCounter.daysTimeCounters.addAll(daysTimeCounters);
			weekCounter.timeCounter.totalTimes = new TimeDuration(totalTimes);
			weekCounter.timeCounter.normalTimes = new TimeDuration(computeNormalTimesByWeek(totalTimes));
			weekCounter.timeCounter.extraTimes125 = new TimeDuration(computeExtraTimes(totalTimes, TimerHelper.ExtraTimeType.AT_125));
			weekCounter.timeCounter.extraTimes150 = new TimeDuration(computeExtraTimes(totalTimes, TimerHelper.ExtraTimeType.AT_150));
			weekCounter.timeCounter.nightlyTimes = new TimeDuration(nightlyHours);
			weekCounter.timeCounter.sundayTimes = new TimeDuration(sundayTime);
			weekCounter.timeCounter.dayOffTimes = new TimeDuration(computeDayOffTimes(weekDays));
			weeksCounter.add(weekCounter);
			pay.weeksCounters.add(weekCounter);
			monthTotalTimes = monthTotalTimes.plus(weekCounter.timeCounter.totalTimes.duration);
			monthNormalTimes = monthNormalTimes.plus(weekCounter.timeCounter.normalTimes.duration);
			monthExtraTimes125 = monthExtraTimes125.plus(weekCounter.timeCounter.extraTimes125.duration);
			monthExtraTimes150 = monthExtraTimes150.plus(weekCounter.timeCounter.extraTimes150.duration);
			monthNightlyTimes = monthNightlyTimes.plus(weekCounter.timeCounter.nightlyTimes.duration);
			monthSundayTimes = monthSundayTimes.plus(weekCounter.timeCounter.sundayTimes.duration);
			monthDayOffTimes = monthDayOffTimes.plus(weekCounter.timeCounter.dayOffTimes.duration);
		}
		pay.monthTimeCounter.totalTimes = new TimeDuration(monthTotalTimes);
		pay.monthTimeCounter.normalTimes = new TimeDuration(monthNormalTimes);
		pay.monthTimeCounter.extraTimes125 = new TimeDuration(monthExtraTimes125);
		pay.monthTimeCounter.extraTimes150 = new TimeDuration(monthExtraTimes150);
		pay.monthTimeCounter.nightlyTimes = new TimeDuration(monthNightlyTimes);
		pay.monthTimeCounter.sundayTimes = new TimeDuration(monthSundayTimes);
		pay.monthTimeCounter.dayOffTimes = new TimeDuration(monthDayOffTimes);

		return pay;
	}

	public static Duration computeNormalTimesByDay(Duration total) {
		return total.isShorterThan(TimerHelper.DAY_DURATION) ? total : TimerHelper.DAY_DURATION;
	}

	public static Duration computeNormalTimesByWeek(Duration total) {
		return total.isShorterThan(TimerHelper.WEEK_DURATION) ? total : TimerHelper.WEEK_DURATION;
	}

	private static Duration computeExtraTimes(final Duration total, final TimerHelper.ExtraTimeType extraTimeType) {
		Duration extraTime = Duration.ZERO;
		switch (extraTimeType) {
			case AT_125:
				if (total.isLongerThan(extraTimeType.min) && total.isShorterThan(extraTimeType.max)) {
					extraTime = total.minus(extraTimeType.min);
				} else if (total.isLongerThan(extraTimeType.max)) {
					extraTime = extraTimeType.max.minus(extraTimeType.min);
				} else {
					extraTime = Duration.ZERO;
				}
				break;
			case AT_150:
				extraTime = total.isLongerThan(extraTimeType.min) ? total.minus(extraTimeType.min) : Duration.ZERO;
				break;
		}
		return extraTime;
	}

	private static Duration computeDayOffTimes(final DateTime dt, final F.Tuple3<Duration, Duration, Duration> d) {
		Duration duration = Duration.ZERO;
		if (DaysOff.isDayOff(dt)) {
			duration = duration.plus(d._1).plus(d._2);
		}
		return duration;
	}

	private static Duration computeDayOffTimes(Set<Day> week) {
		Duration duration = Duration.ZERO;
		for (Day day : week) {
			if (DaysOff.isDayOff(day.date)) {
				F.Tuple3<Duration, Duration, Duration> t = day.computeDurations();
				duration = duration.plus(t._1).plus(t._2);
			}
		}
		return duration;
	}

	/**
	 * Return all days of a month/year from Cra split by week
	 *
	 * @param trigramme trigamme of the employee
	 * @param year      the year of the pay
	 * @param month     the month of the pay
	 * @return a <code></code>Map</code> which contains for each monday of week a set of all days of this week
	 */
	private static Map<DateTime, Set<Day>> findDaysByWeeks(final String trigramme, final Integer year, final Integer month) {
		final List<DateTime> weeksDays = Lists.newArrayListWithExpectedSize(28);
		//final Set<DateTime> weeksDays = Sets.newTreeSet(DateTimeComparator.getDateOnlyInstance());
		DateTime firstDay = new DateTime(year, month, 1, 0, 0);
		DateTime monday = TimerHelper.getMondayOfDate(firstDay);
		DateTime lastDay = TimerHelper.getLastDayOfMonth(year, month);
		TimerHelper timerHelper = new TimerHelper(year, TimerHelper.MonthType.getById(month));
		int i = 0;
		while (!monday.isAfter(lastDay)) {
			weeksDays.addAll(timerHelper.getWeekDays(monday));
			i++;
			monday = monday.plusWeeks(i);
		}

		Collections.sort(weeksDays, DateTimeComparator.getDateOnlyInstance());

		List<Day> days = Cra.findDaysByTrigrammeBetweenDates(trigramme, weeksDays.get(0), weeksDays.get(weeksDays.size() - 1));

		Map<DateTime, Set<Day>> result = Maps.newTreeMap();
		for (Day day : days) {

			final DateTime mondayOfWeek = TimerHelper.getMondayOfDate(day.date);
			if (!result.containsKey(mondayOfWeek)) {
				result.put(mondayOfWeek, new TreeSet<Day>(Day.BY_DATE));
			}
			result.get(mondayOfWeek).add(day);
		}

		return result;
	}


}
