package controllers;

import be.objectify.deadbolt.actions.And;
import be.objectify.deadbolt.actions.Restrictions;
import dto.partTime.PartTimeDataTableDTO;
import helpers.SecurityHelper;
import models.Employee;
import models.PartTime;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import security.RoleName;

import static play.libs.Json.toJson;

/**
 * @author leo
 *         Date: 15/12/12
 *         Time: 16:00
 */
public class PartsTimes extends Controller {

	/**
	 * getPartTime
	 */
	@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
	public static Result getPartTime() {
		return ok(toJson(new PartTimeDataTableDTO(SecurityHelper.findEmployeeByConnectedUser().partTimes)));
	}

	/**
	 * isEnabled
	 */
	@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
	public static Result isPartTimeEnabled() {
		return ok(toJson(SecurityHelper.findEmployeeByConnectedUser().findActivePartTime() != null));
	}

	/**
	 * Add partTime
	 */
	@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
	public static Result addPartTime() {
		final Form<PartTime> form = form(PartTime.class).bind(request().body().asJson());
		if (form.hasErrors()) {
			return badRequest(form.errorsAsJson());
		}
		PartTime partTime = form.get();
		partTime = Employee.addPartTime(SecurityHelper.getConnectedUser().trigramme, partTime);
		return ok(toJson(partTime));
	}

	/**
	 * disable
	 */
	@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
	public static Result disablePartTime() {
		Employee employee = SecurityHelper.findEmployeeByConnectedUser();
		if (employee.partTimes.isEmpty()) {
			return badRequest(toJson("Aucun temps partiel..."));
		}
		Employee.disableActivePartTime(employee.trigramme);
		return ok();
	}
}
