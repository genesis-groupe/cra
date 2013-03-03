package models;

import com.github.jmkgreen.morphia.annotations.*;
import com.github.jmkgreen.morphia.mapping.Mapper;
import com.github.jmkgreen.morphia.query.Query;
import com.github.jmkgreen.morphia.query.UpdateOperations;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import constants.FeeType;
import constants.Genesis;
import constants.MissionCode;
import constants.MissionType;
import helpers.deserializer.ObjectIdDeserializer;
import helpers.serializer.ObjectIdSerializer;
import helpers.time.TimerHelper;
import leodagdag.play2morphia.Model;
import leodagdag.play2morphia.MorphiaPlugin;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author : f.patin
 */
@Entity
@Indexes({
		@Index("employee"),
		@Index("employee, year, _month")
})
public class Cra extends Model {

	@Id
	@JsonSerialize(using = ObjectIdSerializer.class)
	@JsonDeserialize(using = ObjectIdDeserializer.class)
	public ObjectId id;

	public Integer year;

	@Transient
	public Month month;
	private Integer _month;

	public boolean isValidated = Boolean.FALSE;

	public boolean isLocked = Boolean.FALSE;

	public String comment;

	public List<Day> days = Lists.newArrayList();

	/**
	 * Integer = week number
	 */
	public Map<Integer, List<StandBy>> standBys = Maps.newHashMap();

	@Embedded
	public List<Fee> fees = Lists.newArrayList();

	@Transient
	public DayCounter dayCounter = new DayCounter();

	@Reference
	public Employee employee;

	@SuppressWarnings({"unused"})
	@PrePersist
	private void prePersist() {
		if (month != null) {
			_month = month.value;
		}
		// Invalidation du CRA
		invalidate();
	}

	@SuppressWarnings({"unused"})
	@PostLoad
	private void postLoad() {
		if (_month != null) {
			month = new Month(TimerHelper.MonthType.getById(_month));
		}
	}

	@SuppressWarnings({"unused"})
	public Cra() {
	}

	public Cra(final Employee employee, final Integer year, final Month month) {
		this.employee = employee;
		this.year = year;
		this.month = month;
	}

	public Cra(final Employee employee, final Integer year, final Integer month) {
		this(employee, year, new Month(month));
	}

	public static void delete(final String trigramme, final Fee fee) {
		Cra cra = getOrCreate(trigramme, fee.date.getYear(), fee.date.getMonthOfYear());
		Iterables.removeIf(cra.fees, new Predicate<Fee>() {
			@Override
			public boolean apply(@Nullable Fee f) {
				return fee.equals(f);
			}
		});
		cra.update();
	}

	private static Query<Cra> queryToFindMe(final ObjectId id) {
		return MorphiaPlugin.ds().createQuery(Cra.class).field(Mapper.ID_KEY).equal(id);
	}

	private static Query<Cra> queryToFindMe(final String trigramme, final Integer year, final Integer month) {
		return MorphiaPlugin.ds().createQuery(Cra.class)
				.field("employee").equal(Employee.findByTrigramme(trigramme))
				.field("year").equal(year)
				.field("_month").equal(month);
	}

	private static Model.Finder<ObjectId, Cra> find() {
		return new Model.Finder<>(ObjectId.class, Cra.class);
	}

	public static Boolean existForEmployee(Employee employee) {
		return find().field("employee").equal(employee).countAll() > 0;
	}

	public static Cra create(final String trigramme, final Integer year, final Integer month) {
		return new Cra(Employee.findByTrigramme(trigramme), year, new Month(month)).insert();
	}

	public static Cra validate(ObjectId id) {
		UpdateOperations<Cra> op = MorphiaPlugin.ds().createUpdateOperations(Cra.class).set("isValidated", true);
		return MorphiaPlugin.ds().findAndModify(queryToFindMe(id), op);
	}

	public static Cra invalidate(ObjectId id) {
		UpdateOperations<Cra> op = MorphiaPlugin.ds().createUpdateOperations(Cra.class)
				.set("isValidated", false)
				.set("isLocked", false);
		return MorphiaPlugin.ds().findAndModify(queryToFindMe(id), op);
	}

	private void invalidate() {
		this.isValidated = Boolean.FALSE;
		this.isLocked = Boolean.FALSE;
		this.computeFees(Boolean.TRUE);
		Pay.invalidate(this.employee.trigramme, this.year, this._month);
	}

	public static void deleteDay(final String trigramme, final Integer year, final Integer month, final DateTime date) {
		final Cra cra = Cra.queryToFindMe(trigramme, year, month).get();
		// Suppression des jours à mettre à jour
		Iterables.removeIf(cra.days, new Predicate<Day>() {
			@Override
			public boolean apply(@Nullable Day day) {
				return date.isEqual(day.date);
			}
		});
		// Mise à jour du CRA
		cra.update();
	}

	public static void deleteHolidays(final String trigramme, final DateTime start, final DateTime end) {
		Query<Cra> q = find()
				.field("employee").equal(Employee.findByTrigramme(trigramme));
		q.and(
				q.and(
						q.criteria("year").equal(start.getYear()),
						q.criteria("_month").greaterThanOrEq(start.getMonthOfYear())
				),
				q.and(
						q.criteria("year").equal(end.getYear()),
						q.criteria("_month").lessThanOrEq(end.getMonthOfYear())
				)
		);

		List<Cra> cras = q.asList();
		for (Cra cra : cras) {
			Boolean deleteMorning = Boolean.FALSE;
			Boolean deleteAfternoon = Boolean.FALSE;
			Collections.sort(cra.days, Day.BY_DATE);
			for (Day day : cra.days) {
				if ((day.date.isBefore(start))) {
					continue;
				}
				if (day.date.isAfter(end)) {
					break;
				}
				if (day.morning instanceof Holiday) {
					day.morning = null;
					deleteMorning = Boolean.TRUE;
				}
				if (day.afternoon instanceof Holiday) {
					day.afternoon = null;
					deleteAfternoon = Boolean.TRUE;
				}
			}

			if (deleteMorning && deleteAfternoon) {
				// Suppression des jours à mettre à jour
				Iterables.removeIf(cra.days, new Predicate<Day>() {
					@Override
					public boolean apply(@Nullable Day day) {
						return day.morning == null && day.afternoon == null;
					}
				});
			}
			if (deleteMorning || deleteAfternoon) {
				// Mise à jour du CRA
				cra.update();
			}
		}
	}

	public static void saveComment(ObjectId id, String comment) {
		UpdateOperations<Cra> op = MorphiaPlugin.ds().createUpdateOperations(Cra.class).set("comment", comment);
		MorphiaPlugin.ds().findAndModify(queryToFindMe(id), op);
	}

	public static void deleteComment(ObjectId id) {
		UpdateOperations<Cra> op = MorphiaPlugin.ds().createUpdateOperations(Cra.class).unset("comment");
		MorphiaPlugin.ds().findAndModify(queryToFindMe(id), op);
	}

	public static void saveStandBy(final String trigramme, final Integer year, final Integer month, final Integer week, final List<StandBy> standBys) {

		Cra cra = queryToFindMe(trigramme, year, month).get();
		if (cra != null) {
			if (!cra.standBys.containsKey(week)) {
				cra.standBys.put(week, new ArrayList<StandBy>());
			} else {
				cra.standBys.get(week).clear();
			}
			cra.standBys.get(week).addAll(standBys);
			// Mise à jour du CRA
			cra.update();
		}
	}

	public static Cra getOrCreate(String trigramme, Integer year, Integer month) {
		Cra cra = Cra.findByTrigrammeAndYearAndMonth(trigramme, year, month);
		if (cra == null) {
			cra = Cra.create(trigramme, year, month);
			cra = cra.updateDays(PartTime.extract(Employee.findByTrigramme(trigramme).findActivePartTime(), year, month));
		}
		return cra;
	}

	public static Cra findById(ObjectId id) {
		return find().byId(id);
	}

	public static Cra fetch(final String trigramme, final Integer year, final Integer month, final boolean withPastAndFuture) {
		final TimerHelper monthHelper = new TimerHelper(year, month);
		final List<DateTime> dts;
		if (withPastAndFuture) {
			dts = monthHelper.completeToCalendar(monthHelper.getMonthDays());
		} else {
			dts = monthHelper.getMonthDays();
		}

		DateTime firstDay = dts.get(0);
		DateTime lastDay = dts.get(dts.size() - 1);

		// Retrieve CRA from asked Month/Year
		Cra cra = Cra.getOrCreate(trigramme, year, month);

		// Retrieve CRA from previous Month
		if (firstDay.getMonthOfYear() != month || firstDay.getYear() != year) {
			Cra previousCra = queryToFindMe(trigramme, firstDay.getYear(), firstDay.getMonthOfYear())
					.retrievedFields(true, "days")
					.get();
			if (previousCra != null) {
				cra.days.addAll(previousCra.days);
			}
		}
		// Retrieve CRA from next Month
		if (lastDay.getMonthOfYear() != month || lastDay.getYear() != year) {
			Cra nextCra = queryToFindMe(trigramme, lastDay.getYear(), lastDay.getMonthOfYear())
					.retrievedFields(true, "days")
					.get();
			if (nextCra != null) {
				cra.days.addAll(nextCra.days);
			}
		}

		// Select desired days for CRA
		Iterables.removeIf(cra.days, new Predicate<Day>() {
			@Override
			public boolean apply(@Nullable Day day) {
				return !dts.contains(day.date);
			}
		});
		// Remove cra days from All dates
		final List<DateTime> craDays = Lists.transform(cra.days, Day.DAY_2_DATE_TIME_FUNCTION);
		Iterables.removeIf(dts, new Predicate<DateTime>() {
			@Override
			public boolean apply(@Nullable DateTime dateTime) {
				return craDays.contains(dateTime);
			}
		});
		// Add missing empty Day to Cra
		cra.days.addAll(Lists.transform(dts, Day.DATE_TIME_2_DAY_FUNCTION));
		//Set past or future
		setInPastOrFuture(cra, monthHelper);
		// Sort by Cra.days.date
		if (!Iterables.isEmpty(cra.days)) {
			Collections.sort(cra.days, Day.BY_DATE);
		}
		// Sort
		for (Integer week : cra.standBys.keySet()) {
			Collections.sort(cra.standBys.get(week), StandBy.BY_INDEX);
		}
		//Compute counter
		cra.computeMonthDayCounter(monthHelper);
		// Determine if Pay is already validated
		cra.isLocked = Pay.isValidated(trigramme, year, month);
		return cra;
	}

	public static Cra fetch(final String trigramme, final Integer year, final Integer month, final Customer customer, final boolean withPastAndFuture) {
		Cra cra = fetch(trigramme, year, month, withPastAndFuture);
		for (Day day : cra.days) {
			day.morning = HalfDay.filterByCustomer(customer, day.morning);
			day.afternoon = HalfDay.filterByCustomer(customer, day.afternoon);
		}
		cra.standBys = StandBy.filterByCustomer(customer, cra.standBys);
		return cra;
	}

	public static Cra findByTrigrammeAndYearAndMonth(String trigramme, Integer year, Integer month) {
		return queryToFindMe(trigramme, year, month).get();
	}

	public static List<Holiday> getHolidays(String trigramme, Integer year, Boolean withRecurrent) {
		Query<Cra> q = find()
				.field("employee").equal(Employee.findByTrigramme(trigramme))
				.retrievedFields(false, "employee");

		q.criteria("year").greaterThanOrEq(year - 1);

		List<Cra> cras = q.asList();
		List<Holiday> holidays = Lists.newArrayList();
		for (Cra cra : cras) {
			for (Day day : cra.days) {
				if (day.morning instanceof Holiday) {
					holidays.add((Holiday) day.morning);
				}
				if (day.afternoon instanceof Holiday) {
					holidays.add((Holiday) day.afternoon);
				}
			}
		}
		Collections.sort(holidays, Holiday.HOLIDAY_COMPARATOR);
		return holidays;
	}

	public static List<Day> findDaysByTrigrammeBetweenDates(final String trigramme, final DateTime start, final DateTime end, final Boolean morning, final Boolean afternoon) {
		if (start.isEqual(end)) {
			Cra cra = Cra.findByTrigrammeAndYearAndMonth(trigramme, start.getYear(), start.getDayOfMonth());
			if (cra == null) {
				return Collections.emptyList();
			} else {
				return Lists.newArrayList(Iterables.filter(cra.days, new Predicate<Day>() {
					@Override
					public boolean apply(@Nullable final Day day) {
						if (day.date.isEqual(start)) {
							if (Boolean.TRUE.equals(morning) && day.morning != null) {
								return false;
							} else if (Boolean.TRUE.equals(afternoon) && day.afternoon != null) {
								return false;
							}
						}
						return true;
					}
				}));
			}
		} else {

			List<DateTime> dates = Lists.newArrayList(start, end);
			List<Day> days = Lists.newArrayListWithExpectedSize(28);
			Integer year = null;
			Integer month = null;
			for (DateTime date : dates) {
				if (year == null && month == null) {
					year = date.getYear();
					month = date.getMonthOfYear();
				} else if ((!year.equals(date.getYear())) || (!month.equals(date.getMonthOfYear()))) {
					year = date.getYear();
					month = date.getMonthOfYear();
				} else {
					continue;
				}

				Cra cra = Cra.findByTrigrammeAndYearAndMonth(trigramme, year, month);
				if (cra != null) {
					Collections.sort(cra.days, Day.BY_DATE);
					days.addAll(Collections2.filter(cra.days, new Predicate<Day>() {
						@Override
						public boolean apply(@Nullable Day day) {
							if (day.date.isEqual(start)) {
								if (Boolean.TRUE.equals(morning) && day.morning != null) {
									return true;
								} else if (day.afternoon != null) {
									return true;
								} else {
									return false;
								}
							} else if (day.date.isEqual(end)) {
								if (day.morning != null) {
									return true;
								} else if (Boolean.TRUE.equals(afternoon) && day.afternoon != null) {
									return true;
								} else {
									return false;
								}

							} else {
								return (day.date.isAfter(start) && day.date.isBefore(end));
							}
						}
					}));
				}
			}
			return days;
		}
	}

	public static List<Day> findDaysByTrigrammeBetweenDates(final String trigramme, final DateTime start, final DateTime end) {
		List<DateTime> dates = Lists.newArrayList(start, end);
		List<Day> days = Lists.newArrayListWithExpectedSize(28);
		Integer year = null;
		Integer month = null;
		for (DateTime date : dates) {
			if (year == null && month == null) {
				year = date.getYear();
				month = date.getMonthOfYear();
			} else if ((date.getYear() != year) || (date.getMonthOfYear() != month)) {
				year = date.getYear();
				month = date.getMonthOfYear();
			} else {
				continue;
			}

			Cra cra = Cra.getOrCreate(trigramme, year, month);

			days.addAll(Collections2.filter(cra.days, new Predicate<Day>() {
				@Override
				public boolean apply(@Nullable Day day) {
					return (!day.date.isBefore(start))
							&& (!day.date.isAfter(end));
				}
			}));
		}
		return days;
	}

	private static void setInPastOrFuture(final Cra cra, final TimerHelper th) {
		for (Day day : cra.days) {
			day.isInPastOrFuture = th.isInPastOrFuture(day.date);
		}
	}



	public static List<Customer> fetchCustomers(final String trigramme, final Integer year, final Integer month) {
		final Cra cra = Cra.queryToFindMe(trigramme, year, month).get();
		if (cra == null) {
			return new ArrayList<>();
		}

		List<ObjectId> customersId = Lists.newArrayList();
		for (Day day : cra.days) {
			customersId.addAll(HalfDay.extractCustomerId(day.morning));
			customersId.addAll(HalfDay.extractCustomerId(day.afternoon));
		}
		Set<Customer> customers = Sets.newTreeSet(Customer.BY_NAME);
		for (ObjectId id : customersId) {
			if (id != null) {
				customers.add(Customer.findById(id));
			}
		}

		return Lists.newArrayList(Collections2.filter(customers, Genesis.WITHOUT_GENESIS));
	}

	public static void updatePartTime(Employee employee, PartTime partTime) {
		removeExistingPartTimes(employee, partTime.startPartTime);
		addPartsTimes(employee, partTime);
	}

	public void addStandBy(final Integer week, final StandBy standBy) {
		if (!this.standBys.keySet().contains(week)) {
			this.standBys.put(week, new ArrayList<StandBy>());
		}
		this.standBys.get(week).add(standBy);
	}

	public Cra updateDays(final List<Day> days) {
		if (days == null || Iterables.isEmpty(days)) {
			return this;
		}
		// Extraction des dates à traiter
		final List<DateTime> dates = Lists.transform(days, Day.DAY_2_DATE_TIME_FUNCTION);
		// Suppression des jours à mettre à jour
		Iterables.removeIf(this.days, new Predicate<Day>() {
			@Override
			public boolean apply(@Nullable Day day) {
				return dates.contains(day.date);
			}
		});
		// Mise à jour du CRA
		this.update();
		// Insertion des nouvelles journées
		UpdateOperations<Cra> addOp = MorphiaPlugin.ds().createUpdateOperations(Cra.class).addAll("days", days, true);
		return MorphiaPlugin.ds().findAndModify(queryToFindMe(this.id), addOp);
	}

	public Cra createDays(final List<Day> days) {
		if (days == null || Iterables.isEmpty(days)) {
			return this;
		}

		if (this.days == null || Iterables.isEmpty(this.days)) {
			this.days = days;
		} else {
			// Extraction des dates à traiter
			final List<DateTime> dates = Lists.transform(days, Day.DAY_2_DATE_TIME_FUNCTION);

			for (Day day : days) {
				boolean found = false;
				for (Day cDay : this.days) {
					if (day.date.isEqual(cDay.date)) {
						found = true;
						if (day.morning != null) {
							cDay.morning = day.morning;
						}
						if (day.afternoon != null) {
							cDay.afternoon = day.afternoon;
						}
						break;
					}
				}
				if (!found) {
					this.days.add(day);
				}
			}
		}
		return this.update();
		/*// Extraction des dates à traiter
		final List<DateTime> dates = Lists.transform(days, Day.DAY_2_DATE_TIME_FUNCTION);
		// Suppression des jours à mettre à jour
		Iterables.removeIf(this.days, new Predicate<Day>() {
			@Override
			public boolean apply(@Nullable Day day) {
				return dates.contains(day.date);
			}
		});
		// Mise à jour du CRA
		this.update();
		// Insertion des nouvelles journées
		UpdateOperations<Cra> addOp = MorphiaPlugin.ds().createUpdateOperations(Cra.class).addAll("days", days, true);
		return MorphiaPlugin.ds().findAndModify(queryToFindMe(this.id), addOp);*/
	}

	public Cra updateDay(Day day) {
		return updateDays(Arrays.asList(day));
	}

	public DayCounter computeMonthDayCounter(final TimerHelper th) {
		this.dayCounter.nbTheoreticalWorkingDays = th.getNbTheoreticalWorkingDays();
		for (Day day : this.days) {
			if (th.isInMonth(day.date)) {
				this.dayCounter.add(day.computeDayCounter());
			}
		}
		return this.dayCounter;
	}

	public List<MissionCounter> computeMissionCounter(final MissionType missionType) {
		final Map<ObjectId, List<MissionCounter>> result = Maps.newHashMap();
		for (Day day : this.days) {
			Map<ObjectId, List<MissionCounter>> map = day.computeMissionsCounter(missionType);
			MissionCounter.merge(map, result);
		}
		return MissionCounter.flatten(result);
	}

	public static List<Fee> computeFees(final String trigramme, final Integer year, final Integer month, final Boolean includingMissionAllowance) {
		Cra cra = Cra.findByTrigrammeAndYearAndMonth(trigramme, year, month);
		return cra != null ? cra.computeFees(includingMissionAllowance) : new ArrayList<Fee>();
	}

	private List<Fee> computeFees(final Boolean includingMissionAllowance) {
		// Delete all Mission ALLOWANCE
		Iterables.removeIf(this.fees, new Predicate<Fee>() {
			@Override
			public boolean apply(@Nullable Fee fee) {
				return FeeType.MISSION_ALLOWANCE.equals(fee.type);
			}
		});
		if (Boolean.TRUE.equals(includingMissionAllowance)) {
			//Compute all Mission ALLOWANCE
			for (Day day : this.days) {
				this.fees.addAll(day.computeMissionAllowance(this.employee));
			}
		}
		return fees;
	}

	public static void addPartsTimes(Employee employee, PartTime partTime) {
		for (int i = 0; i < 12; i++) {
			final DateTime startDate = partTime.startPartTime.plusMonths(i);
			if (partTime.endPartTime == null || !startDate.isAfter(partTime.endPartTime)) {
				final int year = startDate.getYear();
				final int month = startDate.getMonthOfYear();
				Cra cra = queryToFindMe(employee.trigramme, year, month).get();
				if (cra == null) {
					cra = new Cra(employee, year, month);
				}
				cra.updateDays(PartTime.extract(partTime, year, month));
			}
		}
	}

	public static void removeExistingPartTimes(final Employee employee, final DateTime startTime) {

		List<Cra> cras = Cra.find()
				.field("employee").equal(employee)
				.field("year").greaterThanOrEq(startTime.getYear())
				.asList();
		for (Cra cra : cras) {
			if (cra.year == startTime.getYear() && cra._month >= startTime.getMonthOfYear()) {
				removeHalfDays(cra, MissionCode.TP, startTime);
			}
			if (cra.year > startTime.getYear()) {
				removeHalfDays(cra, MissionCode.TP, startTime);
			}
		}
	}

	private static void removeHalfDays(final Cra cra, final MissionCode missionCode, final DateTime startTime) {
		Boolean toUpdate = Boolean.FALSE;
		for (Day day : cra.days) {
			if (day.date.isAfter(startTime) || day.date.isEqual(startTime)) {
				if (!day.morning.isSpecial() && missionCode.name().equals(day.morning.mission.code)) {
					day.morning = null;
					toUpdate = Boolean.TRUE;
				}
				if (!day.afternoon.isSpecial() && missionCode.name().equals(day.afternoon.mission.code)) {
					day.afternoon = null;
					toUpdate = Boolean.TRUE;
				}
			}
		}
		Iterables.removeIf(cra.days, new Predicate<Day>() {
			@Override
			public boolean apply(@Nullable Day day) {
				return day.morning == null && day.afternoon == null;
			}
		});
		if (toUpdate) {
			if (cra.days == null || Iterables.isEmpty(cra.days)) {
				cra.delete();
			} else {
				cra.update();
			}
		}
	}

	public BigDecimal totalInRealDay() {
		BigDecimal result = BigDecimal.ZERO;
		for (Day day : this.days) {
			result = result.add(HalfDay.totalInRealDay(day.morning)).add(HalfDay.totalInRealDay(day.afternoon));
		}
		return result;
	}

	public BigDecimal totalInRealDay(final MissionType missionType) {
		BigDecimal result = BigDecimal.ZERO;
		for (Day day : this.days) {
			result = result.add(HalfDay.totalInRealDay(day.morning, missionType)).add(HalfDay.totalInRealDay(day.afternoon, missionType));
		}
		return result;
	}

}


