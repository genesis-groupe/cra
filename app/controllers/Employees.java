package controllers;

import be.objectify.deadbolt.actions.And;
import be.objectify.deadbolt.actions.Restrictions;
import cache.UserCache;
import com.github.jmkgreen.morphia.annotations.Id;
import dto.common.IdDTO;
import dto.employee.AffectedMissionDTO;
import dto.employee.EmployeeLightDTO;
import dto.employee.EmployeesDTO;
import helpers.ErrorMsgFactory;
import helpers.SecurityHelper;
import helpers.binder.ObjectIdW;
import helpers.deserializer.ObjectIdDeserializer;
import models.Employee;
import models.Mission;
import models.Role;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import play.data.Form;
import play.libs.F;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import security.RoleName;
import security.Secured;
import vendor.CSV;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;
import static play.libs.Json.toJson;

/**
 * @author f.patin
 */
@Security.Authenticated(Secured.class)
public class Employees extends AbstractController {

	public static class UpdateEmployeeForm extends UpdateForm {

		@Id
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		public ObjectId id;

		public String username;

		public String password;

		public String email;

		public String trigramme;

		public String firstName;

		public String lastName;

		public Boolean isManager;

		public Role role;

		public String validate() {
			return role.id == null ? "Role inconnu" : null;
		}

		@Override
		protected void ensureDbField() {
			role = Role.findbyId(role.id);
		}
	}

	public static class CreateEmployeeForm implements CreateForm<Employee> {

		public String username;

		public String password;

		public String email;

		public String trigramme;

		public String firstName;

		public String lastName;

		public Boolean isManager;

		public Role role;

		public String validate() {
			role = Role.findbyId(role.id);
			return role == null ? "Role inconnu" : null;
		}

		public Employee to() {
			Employee employee = new Employee(username);
			employee.email = email;
			employee.trigramme = trigramme;
			employee.firstName = firstName;
			employee.lastName = lastName;
			employee.isManager = isManager;
			employee.role = role;
			employee.password = SecurityHelper.getDefaultEncryptedPassword();
			return employee;
		}
	}

	@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
	public static Result fetchMissions(final String trigramme, final Integer year, final Integer month) {
		checkNotNull(trigramme, "trigramme must not be null");
		return ok(toJson(Employee.fetchCurrentMissions(trigramme, year, month, Boolean.FALSE)));
	}

	@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
	public static Result fetchMissionsWithStandby(final String trigramme, final Integer year, final Integer week) {
		checkNotNull(trigramme, "trigramme must not be null");
		checkNotNull(year, "year must not be null");
		checkNotNull(week, "week must not be null");
		return ok(toJson(Employee.fetchCurrentMissionsWithStandby(trigramme, year, week)));
	}

	@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
	public static Result fetchFeesMission(final String trigramme) {
		checkNotNull(trigramme, "trigramme must not be null");
		return ok(toJson(Employee.fetchFeesMission(trigramme)));
	}

	@Restrictions({@And(RoleName.ADMINISTRATOR)})
	public static Result all() {
		return ok(toJson(EmployeesDTO.of(Employee.all())));
	}

	@Restrictions({@And(RoleName.ADMINISTRATOR)})
	public static Result fetch(final String trigramme) {
		return ok(toJson(EmployeeLightDTO.of(Employee.findByTrigramme(trigramme))));
	}

	@Restrictions({@And(RoleName.ADMINISTRATOR)})
	public static Result update(final ObjectIdW id) throws IllegalAccessException {
		final Form<UpdateEmployeeForm> form = form(UpdateEmployeeForm.class).bind(request().body().asJson());
		if (form.hasErrors()) {
			return badRequest(form.errorsAsJson());
		}
		final UpdateEmployeeForm updateEmployeeForm = form.get();

		final Employee employee = Employee.updateFields(id.value, updateEmployeeForm.fields());
		if (UserCache.getInstance().getByUsername(employee.username) == null) {
			UserCache.getInstance().invalidate(employee.username);
		} else {
			UserCache.getInstance().invalidateAll();
		}
		return ok(toJson(EmployeesDTO.of(employee)));
	}

	@Restrictions({@And(RoleName.ADMINISTRATOR)})
	@BodyParser.Of(BodyParser.Json.class)
	public static Result addAffectedMission(final ObjectIdW id) {
		final Form<IdDTO> form = form(IdDTO.class).bind(request().body().asJson());
		if (form.hasErrors()) {
			return badRequest(form.errorsAsJson());
		}
		final ObjectId missionId = form.get().id;
		final Mission mission = Mission.findById(missionId);
		return ok(toJson(Employee.addMission(id.value, mission)));
	}

	@Restrictions({@And(RoleName.ADMINISTRATOR)})
	public static Result removeAffectedMission(final ObjectIdW id, final ObjectIdW affectedMissionId) {
		return ok(toJson(AffectedMissionDTO.of(Employee.findById(id.value).removeAffectedMission(affectedMissionId.value))));
	}

	@Restrictions({@And(RoleName.ADMINISTRATOR)})
	public static Result create() throws IllegalAccessException {
		final Form<CreateEmployeeForm> form = form(CreateEmployeeForm.class).bind(request().body().asJson());
		if (form.hasErrors()) {
			return badRequest(form.errorsAsJson());
		}
		CreateEmployeeForm createEmployeeForm = form.get();
		return ok(toJson(EmployeeLightDTO.of(createEmployeeForm.to().<Employee>insert())));
	}

	@Restrictions({@And(RoleName.ADMINISTRATOR)})
	public static Result upload() {
		Http.MultipartFormData body = request().body().asMultipartFormData();
		Http.MultipartFormData.FilePart employeesFile = body.getFile("employees");
		if (employeesFile != null) {
			String fileName = employeesFile.getFilename();
			String contentType = employeesFile.getContentType();
			File file = employeesFile.getFile();
			F.Tuple<Integer, Integer> csv = CSV.load(file);
			return ok(toJson(String.format("File uploaded [%s] - Result:[%s/%s]", fileName, csv._1, csv._2)));
		} else {
			return badRequest(ErrorMsgFactory.asJson("error", "Missing file"));
		}
	}
}


