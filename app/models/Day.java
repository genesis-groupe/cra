package models;

import cache.FeeParameterCache;
import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.PostLoad;
import com.github.jmkgreen.morphia.annotations.PrePersist;
import com.github.jmkgreen.morphia.annotations.Transient;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import constants.FeeType;
import constants.MissionType;
import constants.Pattern;
import constants.Vehicles.VehicleType;
import helpers.deserializer.DateTimeDeserializer;
import helpers.formatter.JodaDateTimeFormatter;
import helpers.serializer.DateTimeSerializer;
import helpers.time.TimerHelper;
import leodagdag.play2morphia.Model;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.joda.time.Duration;
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
public class Day extends Model {

	public static final Comparator<Day> BY_DATE = new Comparator<Day>() {
		@Override
		public int compare(Day day1, Day day2) {
			return DateTimeComparator.getDateOnlyInstance().compare(day1.date, day2.date);
		}
	};
	public static final Function<DateTime, Day> DATE_TIME_2_DAY_FUNCTION = new Function<DateTime, Day>() {
		@Override
		public Day apply(@Nullable DateTime dateTime) {
			return new Day(dateTime);
		}
	};
	public static final Function<Day, DateTime> DAY_2_DATE_TIME_FUNCTION = new Function<Day, DateTime>() {
		@Override
		public DateTime apply(@Nullable Day day) {
			return day.date;
		}
	};

	@Transient
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime date;
	private Date _date;

	@Transient
	public Integer week;

	@Embedded
	public HalfDay morning;

	@Embedded
	public HalfDay afternoon;

	@Transient
	public Boolean isSaturday;

	@Transient
	public Boolean isSunday;

	@Transient
	public Boolean isDayOff;

	@Transient
	public Boolean isInPastOrFuture;

	public List<Fee> missionAllowance = Lists.newArrayList();

	@SuppressWarnings({"unused"})
	@PrePersist
	private void prePersist() {
		if (date != null) {
			_date = date.toDate();
		}
	}

	@SuppressWarnings({"unused"})
	@PostLoad
	private void postLoad() {
		if (_date != null) {
			date = new DateTime(_date.getTime());
			populate();
		}
	}

	@SuppressWarnings({"unused"})
	public Day() {
	}

	public Day(DateTime date) {
		this.date = date;
		populate();
	}

	public boolean isSpecial() {
		return HalfDay.isSpecial(morning) || HalfDay.isSpecial(afternoon);
	}

	public static boolean isSpecial(Day day) {
		return day != null && HalfDay.isSpecial(day.morning) && HalfDay.isSpecial(day.afternoon);
	}

	public F.Tuple3<Duration, Duration, Duration> computeDurations() {
		F.Tuple3<Duration, Duration, Duration> mondayDuration = this.morning != null ? this.morning.computeDuration(this.date) : F.Tuple3(Duration.ZERO, Duration.ZERO, Duration.ZERO);
		F.Tuple3<Duration, Duration, Duration> afternoonDuration = this.afternoon != null ? this.afternoon.computeDuration(this.date) : F.Tuple3(Duration.ZERO, Duration.ZERO, Duration.ZERO);

		return F.Tuple3(
				mondayDuration._1.plus(afternoonDuration._1),
				mondayDuration._2.plus(afternoonDuration._2),
				mondayDuration._3.plus(afternoonDuration._3)
		);
	}

	private void populate() {
		isDayOff = TimerHelper.isDayOff(date);
		isSaturday = TimerHelper.isSaturday(date);
		isSunday = TimerHelper.isSunday(date);
		week = date.getWeekOfWeekyear();
	}

	public DayCounter computeDayCounter() {
		final DayCounter counter = new DayCounter();
		if (this.morning != null) {
			counter.add(this.morning.computeHalfDayCounter());
		}
		if (this.afternoon != null) {
			counter.add(this.afternoon.computeHalfDayCounter());
		}
		return counter;
	}

	public Map<ObjectId, List<MissionCounter>> computeMissionsCounter(final MissionType missionType) {
		final Map<ObjectId, List<MissionCounter>> result = Maps.newHashMap();

		if (this.morning != null) {
			Map<ObjectId, List<MissionCounter>> mc = this.morning.computeMissionsCounter(missionType);
			MissionCounter.merge(mc, result);
		}
		if (this.afternoon != null) {
			Map<ObjectId, List<MissionCounter>> mc = this.afternoon.computeMissionsCounter(missionType);
			MissionCounter.merge(mc, result);
		}
		return result;
	}

	public List<Fee> computeMissionAllowance(Employee employee) {
		missionAllowance.clear();

		Set<Mission> missions = Sets.newHashSet();
		if (this.morning != null) {
			missions.addAll(this.morning.getMissions());
		}
		if (this.afternoon != null) {
			missions.addAll(this.afternoon.getMissions());
		}

		for (Mission mission : missions) {
			final Fee fee = computeMissionAllowance(date, mission, employee);
			if (fee != null) {
				missionAllowance.add(fee);
			}
		}
		return missionAllowance;
	}

	private Fee computeMissionAllowance(final DateTime date, final Mission mission, final Employee employee) {
		switch (mission.allowanceType) {
			case REAL:
				if (employee.vehicle != null) {
					BigDecimal ref;
					if (VehicleType.CAR.equals(employee.vehicle.type)) {
						ref = FeeParameterCache.get(date, date).cars.get(employee.vehicle.power);
					} else {
						ref = FeeParameterCache.get(date, date).motorcycles.get(employee.vehicle.power);
					}
					BigDecimal total = mission.distance.multiply(ref);
					return new Fee(date, FeeType.MISSION_ALLOWANCE, mission, total);
				}
				return null;
			case NONE:
				return null;
			default:
				BigDecimal total = FeeParameterCache.get(date, date).getFeeAllowanceAmount(mission.allowanceType);
				return new Fee(date, FeeType.MISSION_ALLOWANCE, mission, total);
		}
	}
}
