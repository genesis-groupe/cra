package controllers;

import be.objectify.deadbolt.actions.And;
import be.objectify.deadbolt.actions.Restrictions;
import com.google.common.collect.Lists;
import constants.Vehicles;
import dto.account.GlobalSettingDTO;
import helpers.ErrorMsgFactory;
import helpers.SecurityHelper;
import models.Employee;
import models.User;
import models.Vehicle;
import org.apache.commons.validator.routines.EmailValidator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import play.data.Form;
import play.mvc.Result;
import play.mvc.Security;
import security.RoleName;
import security.Secured;
import views.html.users.showAccount;

import static com.google.common.base.Preconditions.checkNotNull;
import static play.libs.Json.toJson;

/**
 * @author f.patin
 *         Date: 15/12/12
 *         Time: 15:58
 */
@Security.Authenticated(Secured.class)
public class Accounts extends AbstractController {

	public static class PasswordForm {

		public String oldPassword;
		public String newPassword;
		public String newPasswordConfirm;

		public String validate() {
			if (!newPassword.equals(newPasswordConfirm)) {
				return "Les mots de passe ne correspondent pas.";
			} else if (newPassword.equals(oldPassword)) {
				return "Le nouveau mot de passe est le même que l'ancien.";
			} else {
				return null;
			}
		}
	}

	/**
	 * Show account page
	 */
	@Security.Authenticated(Secured.class)
	@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
	public static Result showAccount() {
		final User connectedUser = SecurityHelper.getConnectedUser();
		return ok(showAccount.render(connectedUser, Lists.newArrayList(Vehicles.Brand.values()), Employee.getAllManager()));
	}

	@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
	public static Result changePassword() {
		Result result;
		final Form<PasswordForm> form = form(PasswordForm.class).bind(request().body().asJson());
		if (form.hasErrors()) {
			result = badRequest(form.errorsAsJson());
		} else {
			Employee employeeConnected = SecurityHelper.findEmployeeByConnectedUser();
			final PasswordForm passwordForm = form.get();
			if (employeeConnected.password.equals(SecurityHelper.md5(passwordForm.oldPassword))) {
				employeeConnected.password = SecurityHelper.md5(passwordForm.newPassword);
				employeeConnected.update();
				result = ok();
			} else {
				result = badRequest(ErrorMsgFactory.asJson("Le mot de passe indiqué est incorrect."));
			}
		}
		return result;
	}

	/**
	 * fetchVehicle
	 */
	@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
	public static Result fetchVehicle() {
		Vehicle vehicule = Employee.findByTrigramme(SecurityHelper.getConnectedUser().trigramme).vehicle;
		JsonNode jsFees = vehicule == null ? JsonNodeFactory.instance.nullNode() : toJson(vehicule);
		return ok(toJson(jsFees));
	}

	@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
	public static Result saveVehicle() {
		final Form<Vehicle> vehicle = form(Vehicle.class).bind(request().body().asJson());
		if (vehicle.hasErrors()) {
			return badRequest(vehicle.errorsAsJson());
		}
		Employee employee = SecurityHelper.findEmployeeByConnectedUser();
		employee.vehicle = vehicle.get();
		employee.update();
		return ok();
	}

	@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
	public static Result fetchGlobalSetting() {
		Employee employee = SecurityHelper.findEmployeeByConnectedUser();
		GlobalSettingDTO dto = new GlobalSettingDTO();
		dto.email = employee.email;
		dto.manager = employee.manager;
		dto.showStandBy = employee.showStandBy;
		return ok(toJson(dto));
	}

	@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
	public static Result saveManager(final String trigramme) {
		checkNotNull(trigramme, "trigramme must not be null");
		Result result;
		Employee manager = Employee.findByTrigramme(trigramme);
		Employee employeeConnected = SecurityHelper.findEmployeeByConnectedUser();
		if (manager.isManager.equals(Boolean.FALSE)) {
			result = badRequest(ErrorMsgFactory.asJson("Cet employée n'est pas manager"));
		} else {
			employeeConnected.manager = manager;
			employeeConnected.update();
			result = ok();
		}
		return result;
	}

	@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
	public static Result saveEmail(String email) {
		checkNotNull(email, "email must not be null");
		if (!EmailValidator.getInstance().isValid(email)) {
			return badRequest(ErrorMsgFactory.asJson("Veuillez saisir un mail valide. </br>Exemple : p.nom@exemple.com"));
		}
		Employee employeeConnected = SecurityHelper.findEmployeeByConnectedUser();
		employeeConnected.email = email;
		employeeConnected.update();
		return ok();
	}

	@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
	public static Result changeStandByOption(String showStandBy) {
		checkNotNull(showStandBy, "showStandBy must not be null");
		Employee employeeConnected = SecurityHelper.findEmployeeByConnectedUser();
		employeeConnected.showStandBy = Boolean.parseBoolean(showStandBy);
		employeeConnected.update();
		return ok();
	}
}
