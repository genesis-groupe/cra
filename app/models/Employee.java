package models;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Reference;
import com.github.jmkgreen.morphia.mapping.Mapper;
import com.github.jmkgreen.morphia.query.Query;
import com.github.jmkgreen.morphia.query.UpdateOperations;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import constants.MissionCode;
import constants.MissionType;
import helpers.SecurityHelper;
import helpers.time.TimerHelper;
import leodagdag.play2morphia.Model;
import leodagdag.play2morphia.MorphiaPlugin;
import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import play.libs.F;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: f.patin
 * Date: 09/10/12
 * Time: 20:40
 */
@Entity(value = "User")
public class Employee extends User {

	public static final Comparator<Employee> BY_TRIGRAMME = new Comparator<Employee>() {
		@Override
		public int compare(Employee e1, Employee e2) {
			return e1.trigramme.compareTo(e2.trigramme);
		}
	};

	private static Query<Employee> queryFindPartTime(String trigramme) {
		return queryToFindMe(trigramme)
				.field("partTimes.isActive").equal(true);
	}

	@Reference
	public Employee manager;

	public Boolean isManager = Boolean.FALSE;

	@Embedded
	public List<PartTime> partTimes = Lists.newArrayList();

	@Embedded
	public Vehicle vehicle;

	@Embedded
	public List<AffectedMission> affectedMissions = Lists.newArrayList();

	public Boolean showStandBy = Boolean.FALSE;

	private static Query<Employee> queryToFindMe(String trigramme) {
		return MorphiaPlugin.ds()
				.createQuery(Employee.class)
				.disableValidation()
				.field(Mapper.CLASS_NAME_FIELDNAME).equal(Employee.class.getName())
				.field("trigramme").equal(trigramme);
	}

	private static Query<Employee> queryToFindMe(ObjectId id) {
		return MorphiaPlugin.ds().createQuery(Employee.class)
				.field(Mapper.ID_KEY).equal(id);
	}

	private static Model.Finder<ObjectId, Employee> find() {
		return new Model.Finder<>(ObjectId.class, Employee.class);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)/**/
				.addValue(super.toString())
				.add("manager", manager)/**/
				.add("partTimes", partTimes)/**/
				.add("affectedMissions", affectedMissions)/**/
				.toString();
	}

	@SuppressWarnings({"unused"})
	public Employee() {
		this.setDefault();
	}

	public Employee(final String username) {
		this.username = username;
		this.setDefault();
	}

	private void setDefault() {
		this.password = SecurityHelper.getDefaultEncryptedPassword();
		this.addMissions(Mission.getGenesisMissions());
	}

	public static Employee findByTrigramme(String trigramme) {
		return queryToFindMe(trigramme)
				.get();
	}

	public static Employee findById(final ObjectId id) {
		return find().byId(id);
	}

	public PartTime findActivePartTime() {
		return Iterables.find(this.partTimes, new Predicate<PartTime>() {
			@Override
			public boolean apply(@Nullable PartTime partTime) {
				return partTime.isActive;
			}
		}, null);
	}

	public static Employee disableActivePartTime(final String trigramme) {

		MorphiaPlugin.ds().update(queryFindPartTime(trigramme), MorphiaPlugin.ds()
				.createUpdateOperations(Employee.class)
				.disableValidation()
				.set("partTimes.$.isActive", false));
		return queryToFindMe(trigramme).get();
	}

	public static PartTime addPartTime(final String trigramme, final PartTime partTime) {
		Employee employee = disableActivePartTime(trigramme);
		partTime.isActive = true;
		employee.partTimes.add(partTime);
		employee.update();
		Cra.updatePartTime(employee, partTime);
		return partTime;
	}

	public AffectedMission addMission(final Mission mission) {
		final AffectedMission affectedMission = new AffectedMission(mission);
		this.affectedMissions.add(affectedMission);
		return affectedMission;
	}

	public static Employee addMission(final ObjectId id, final Mission mission) {
		UpdateOperations<Employee> addOp = MorphiaPlugin.ds()
				.createUpdateOperations(Employee.class)
				.add("affectedMissions", new AffectedMission(mission), true);
		return MorphiaPlugin.ds().findAndModify(queryToFindMe(id), addOp);
	}

	public void addMissions(final List<Mission> missions) {
		for (Mission mission : missions) {
			this.affectedMissions.add(new AffectedMission(mission));
		}
	}

	public AffectedMission removeAffectedMission(final ObjectId missionId) {
		final Predicate<AffectedMission> affectedMissionToRemove = new Predicate<AffectedMission>() {
			@Override
			public boolean apply(@Nullable AffectedMission affectedMission) {
				return missionId.equals(affectedMission.mission.id);
			}
		};

		final AffectedMission removedAffectedMission = Iterables.find(this.affectedMissions, affectedMissionToRemove);
		Iterables.removeIf(this.affectedMissions, affectedMissionToRemove);
		this.update();

		return removedAffectedMission;
	}

	public static List<AffectedMission> fetchCurrentMissions(final String trigramme, final Integer year, final Integer month, final boolean withHolidayAndCSS) {
		Employee employee = find()
				.retrievedFields(true, "affectedMissions")
				.field("trigramme").equal(trigramme)
				.get();
		final DateTime firstDayOfMonth = new DateTime(year, month, 1, 0, 0);
		final DateTime lastDayOfMonth = TimerHelper.getLastDayOfMonth(year, month);
		Iterables.removeIf(employee.affectedMissions, new Predicate<AffectedMission>() {
			@Override
			public boolean apply(@Nullable AffectedMission affectedMission) {
				if (!withHolidayAndCSS && (MissionType.HOLIDAY.equals(affectedMission.mission.missionType) || MissionCode.CSS.code.equals(affectedMission.mission.code))) {
					return true;
				}
				if (affectedMission.startDate == null && affectedMission.endDate == null) {
					return false;
				}
				if ((affectedMission.startDate == null || affectedMission.startDate.isBefore(firstDayOfMonth))
						&& (affectedMission.endDate == null || affectedMission.endDate.isAfter(lastDayOfMonth))) {
					return false;
				}
				return true;
			}
		});
		Collections.sort(employee.affectedMissions, AffectedMission.BY_CUSTOMER_NAME);
		return employee.affectedMissions;
	}

	public static List<Employee> all() {
		return find()
				.disableValidation()
				.field(Mapper.CLASS_NAME_FIELDNAME).equal(Employee.class.getName())
				.asList();
	}

	public static List<Employee> getOnlyEmployees() {
		return find()
				.disableValidation()
				.field(Mapper.CLASS_NAME_FIELDNAME).equal(Employee.class.getName())
				.field("isManager").equal(Boolean.FALSE)
				.asList();
	}

	public static List<Employee> getAllManager() {
		return find()
				.disableValidation()
				.field(Mapper.CLASS_NAME_FIELDNAME).equal(Employee.class.getName())
				.field("isManager").equal(Boolean.TRUE)
				.asList();
	}

	public static List<AffectedMission> fetchCurrentMissionsWithStandby(final String trigramme, final Integer year, final Integer week) {
		final DateTime firstDayOfWeek = TimerHelper.getFirstDayOfWeek(year, week);
		Employee employee = queryToFindMe(trigramme)
				.retrievedFields(true, "affectedMissions")
				.get();
		List<AffectedMission> missions = Lists.newArrayList();
		missions.addAll(Collections2.filter(employee.affectedMissions, new Predicate<AffectedMission>() {
			@Override
			public boolean apply(@Nullable AffectedMission affectedMission) {
				return !Iterables.isEmpty(affectedMission.mission.standByMoments)
						&& (firstDayOfWeek.isEqual(affectedMission.startDate) || firstDayOfWeek.isAfter(affectedMission.startDate))
						&& (firstDayOfWeek.isBefore(affectedMission.endDate) || firstDayOfWeek.isEqual(affectedMission.endDate));
			}
		}));
		return missions;
	}

	public static List<AffectedMission> fetchFeesMission(String trigramme) {
		Employee employee = queryToFindMe(trigramme)
				.retrievedFields(true, "affectedMissions")
				.get();
		List<AffectedMission> missions = Lists.newArrayList();
		missions.addAll(Collections2.filter(employee.affectedMissions, new Predicate<AffectedMission>() {
			@Override
			public boolean apply(@Nullable AffectedMission affectedMission) {
				return MissionType.CUSTOMER.equals(affectedMission.mission.missionType) || /**/
						MissionType.INTERNAL_WORK.equals(affectedMission.mission.missionType) ||/**/
						MissionType.PRESALES.equals(affectedMission.mission.missionType);
			}
		}));
		Collections.sort(missions, AffectedMission.BY_CUSTOMER_NAME);
		return missions;
	}

	public static void update(Employee employee) {
		MorphiaPlugin.ds()
				.updateFirst(queryToFindMe(employee.id), employee, Boolean.FALSE);
	}

	public static Employee updateFields(ObjectId id, List<F.Tuple<String, Object>> fields) {
		return updateFields(Employee.class, id, fields);
	}

}
