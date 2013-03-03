package models;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.Reference;
import com.github.jmkgreen.morphia.annotations.Transient;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import constants.MissionType;
import helpers.time.TimerHelper;
import leodagdag.play2morphia.Model;
import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.Duration;
import org.joda.time.Interval;
import play.libs.F;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;

/**
 * User: leo
 * Date: 09/10/12
 * Time: 20:41
 */
@Embedded
public class HalfDay extends Model {

	@Reference
	public Mission mission;
	@Embedded
	public List<Period> periods = Lists.newArrayList();

	@JsonProperty("isSpecial")
	public Boolean isSpecial() {
		return !Iterables.isEmpty(periods);
	}

	public HalfDay() {
	}

	public HalfDay(Mission mission) {
		this.mission = mission;
	}

	private static Model.Finder<ObjectId, HalfDay> find() {
		return new Model.Finder<>(ObjectId.class, HalfDay.class);
	}

	public static List<ObjectId> extractCustomerId(HalfDay halfDay) {
		if (halfDay == null) {
			return Collections.emptyList();
		}

		if (halfDay.isSpecial()) {
			return Lists.newArrayList(Collections2.transform(halfDay.periods, new Function<Period, ObjectId>() {
				@Override
				public ObjectId apply(@Nullable Period period) {
					return period.mission.customerId;
				}
			}));
		} else {
			return Lists.newArrayList(halfDay.mission.customerId);
		}
	}

	public static HalfDay filterByCustomer(final Customer customer, final HalfDay halfDay) {
		if (halfDay == null) {
			return null;
		}
		if (halfDay.isSpecial()) {
			Iterables.removeIf(halfDay.periods, new Predicate<Period>() {
				@Override
				public boolean apply(@Nullable Period period) {
					return !period.mission.customerId.equals(customer.id);
				}
			});
			return Iterables.isEmpty(halfDay.periods) ? null : halfDay;
		} else {
			return (halfDay.mission.customerId.equals(customer.id)) ? halfDay : null;
		}
	}

	public static boolean isSpecial(HalfDay halfDay) {
		return halfDay != null && halfDay.isSpecial();
	}

	public F.Tuple3<Duration, Duration, Duration> computeDuration(final DateTime date) {
		Interval AURORA = new Interval(TimerHelper.START_AURORA.toDateTimeToday(), TimerHelper.START_DAY.toDateTimeToday());
		Interval DAY = new Interval(TimerHelper.START_DAY.toDateTimeToday(), TimerHelper.START_DUSK.toDateTimeToday());
		Interval DUSK = new Interval(TimerHelper.START_DUSK.toDateTimeToday(), TimerHelper.START_AURORA.toDateTimeToday().plusDays(1));
		/**
		 * hours = daily,nightly,sunday
		 */
		Duration dailyHours = org.joda.time.Duration.ZERO;
		Duration nightlyHours = org.joda.time.Duration.ZERO;
		Duration sundayTime = org.joda.time.Duration.ZERO;
		if (Boolean.TRUE.equals(this.isSpecial())) {
			for (Period period : this.periods) {
				DateTime start = period.startTime.toDateTimeToday();
				DateTime end = period.endTime.toDateTimeToday();
				Interval interval = new Interval(start, end);
				Interval aurora = interval.overlap(AURORA);
				if (aurora != null) {
					nightlyHours = nightlyHours.plus(aurora.toDuration());
				}
				Interval day = interval.overlap(DAY);
				if (day != null) {
					dailyHours = dailyHours.plus(day.toDuration());
				}
				Interval dusk = interval.overlap(DUSK);
				if (dusk != null) {
					nightlyHours = nightlyHours.plus(dusk.toDuration());
				}
				sundayTime = (date.getDayOfWeek() == DateTimeConstants.SUNDAY) ? sundayTime.plus(new org.joda.time.Duration(start, end)) : sundayTime;
			}
		} else {
			dailyHours = TimerHelper.HALF_DAY_DURATION;
			sundayTime = (date.getDayOfWeek() == DateTimeConstants.SUNDAY) ? TimerHelper.DAY_DURATION : org.joda.time.Duration.ZERO;
		}

		return F.Tuple3(dailyHours, nightlyHours, sundayTime);
	}

	//TODO to refactor
	public DayCounter computeHalfDayCounter() {

		final DayCounter counter = new DayCounter();
		if (Boolean.TRUE.equals(this.isSpecial())) {
			for (Period period : this.periods) {
				BigDecimal d = TimerHelper.toGenesisDay(period.startTime, period.endTime);
				switch (period.mission.missionType) {
					case CUSTOMER:
						counter.addCustomerDays(d);
						break;
					case HOLIDAY:
						counter.addHolidays(d);
						break;
					case INTERNAL_WORK:
						counter.addInternalWorkDays(d);
						break;
					case PRESALES:
						counter.addPresalesDays(d);
						break;
					case UNPAID:
						counter.addUnpaidDays(d);
						break;
				}
			}
		} else {
			switch (this.mission.missionType) {
				case CUSTOMER:
					counter.addCustomerDays(TimerHelper.GENESIS_HALF_DAY);
					break;
				case HOLIDAY:
					counter.addHolidays(TimerHelper.GENESIS_HALF_DAY);
					break;
				case INTERNAL_WORK:
					counter.addInternalWorkDays(TimerHelper.GENESIS_HALF_DAY);
					break;
				case PRESALES:
					counter.addPresalesDays(TimerHelper.GENESIS_HALF_DAY);
					break;
				case UNPAID:
					counter.addUnpaidDays(TimerHelper.GENESIS_HALF_DAY);
					break;
			}
		}
		return counter;
	}

	public Map<ObjectId, List<MissionCounter>> computeMissionsCounter(final MissionType missionType) {
		Map<ObjectId, List<MissionCounter>> map = Maps.newHashMap();

		if (Boolean.TRUE.equals(this.isSpecial())) {
			for (Period period : this.periods) {
				if (missionType == null || period.mission.missionType.equals(missionType)) {
					if (!map.containsKey(period.mission.id)) {
						map.put(period.mission.id, new ArrayList<MissionCounter>());
					}
					map.get(period.mission.id).add(new MissionCounter(period.mission, TimerHelper.toGenesisDay(period.startTime, period.endTime)));
				}
			}
		} else {
			if (missionType == null || this.mission.missionType.equals(missionType)) {
				map.put(this.mission.id, Lists.newArrayList(new MissionCounter(this.mission, TimerHelper.GENESIS_HALF_DAY)));
			}
		}
		return map;
	}

	@JsonIgnore
	public List<Mission> getMissions() {
		Set<Mission> missions = Sets.newHashSet();
		if (this.isSpecial()) {
			missions.addAll(Collections2.transform(periods, new Function<Period, Mission>() {
				@Override
				public Mission apply(@Nullable Period period) {
					return period.mission;
				}
			}));
		} else {
			missions.add(mission);
		}
		return Lists.newArrayList(missions);
	}

	public static BigDecimal totalInRealDay(final HalfDay halfDay) {
		if (halfDay == null) {
			return BigDecimal.ZERO;
		}
		if (halfDay.isSpecial()) {
			return totalInRealDay(halfDay.periods);
		} else {
			return new BigDecimal("0.5");
		}
	}

	private static BigDecimal totalInRealDay(final List<Period> periods) {
		BigDecimal result = BigDecimal.ZERO;
		for (Period period : periods) {
			result = result.add(period.durationInRealDay());
		}
		return result;
	}

	public static BigDecimal totalInRealDay(final HalfDay halfDay, final MissionType missionType) {
		if (halfDay == null) {
			return BigDecimal.ZERO;
		}
		if (halfDay.isSpecial()) {
			return totalInRealDay(halfDay.periods, missionType);
		} else {
			return missionType.equals(halfDay.mission.missionType) ? new BigDecimal("0.5") : BigDecimal.ZERO;
		}
	}

	private static BigDecimal totalInRealDay(final List<Period> periods, final MissionType missionType) {
		BigDecimal result = BigDecimal.ZERO;
		for (Period period : periods) {
			if (missionType.equals(period.mission.missionType)) {
				result = result.add(period.durationInRealDay());
			}
		}
		return result;
	}
}
