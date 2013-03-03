package controllers;

import helpers.ErrorMsgFactory;
import helpers.SecurityHelper;
import models.User;
import play.Routes;
import play.data.Form;
import play.data.validation.Constraints;
import play.mvc.Result;
import play.mvc.Security;
import security.Secured;
import views.html.index;
import views.html.login;

public class Application extends AbstractController {

	public static class LoginForm {
		@Constraints.Required
		public String username;

		@Constraints.Required
		public String password;

		public String validate() {
			if (!User.authenticate(username, password)) {
				return "Le nom d'utilisateur ou le mot de passe saisi est incorrect.";
			}
			return null;
		}
	}

	private final static Form<LoginForm> LOGIN_FORM = form(LoginForm.class);

	@Security.Authenticated(Secured.class)
	public static Result index() {
		final User userConnected = SecurityHelper.getConnectedUser();
		return ok(index.render(userConnected));
	}

	/**
	 * Login page.
	 */
	public static Result login() {
		return ok(login.render(LOGIN_FORM, null));
	}

	public static Result authenticate() {
		Result result;
		final Form<LoginForm> loginForm = form(LoginForm.class).bindFromRequest();
		if (loginForm.hasErrors()) {
			result = badRequest(login.render(loginForm, ErrorMsgFactory.asHtml(loginForm.errors()).get(0)));
		} else {
			session("username", loginForm.get().username);
			result = redirect(routes.Application.index());
		}
		return result;
	}

	public static Result logout() {
		session().clear();
		flash("success", "You've been logged out");
		return redirect(routes.Application.login());
	}

	// -- Javascript routing
	public static Result javascriptRoutes() {
		response().setContentType("text/javascript");
		return ok(
				Routes.javascriptRouter("jsRoutes",
				        /* Routes for Cra */
						controllers.routes.javascript.Cras.fetch(),
						controllers.routes.javascript.Cras.validate(),
						controllers.routes.javascript.Cras.invalidate(),
						controllers.routes.javascript.Cras.save(),
						controllers.routes.javascript.Cras.deleteDay(),
						controllers.routes.javascript.Cras.saveComment(),
						controllers.routes.javascript.Cras.saveStandBys(),
						controllers.routes.javascript.Cras.deleteComment(),
						controllers.routes.javascript.Cras.fetchFees(),
						controllers.routes.javascript.Cras.fetchCustomers(),

                        /* Routes for Pay */
						controllers.routes.javascript.Pays.fetch(),
						controllers.routes.javascript.Pays.validate(),
						controllers.routes.javascript.Pays.invalidate(),

						// Routes for Holiday
						controllers.routes.javascript.Holidays.createOneDayHoliday(),
						controllers.routes.javascript.Holidays.createHolidayPeriod(),
						controllers.routes.javascript.Holidays.createHolidayRecurrent(),
						controllers.routes.javascript.Holidays.fetchHolidays(),
						controllers.routes.javascript.Holidays.deleteHoliday(),
						controllers.routes.javascript.Holidays.holidaysExisteOnDates(),

                        /* Routes for Fees */
						controllers.routes.javascript.Fees.create(),
						controllers.routes.javascript.Fees.fetch(),
						controllers.routes.javascript.Fees.delete(),
						controllers.routes.javascript.Fees.fetchType(),

                        /* Routes for Export */
						controllers.routes.javascript.Exports.craEmployee(),
						controllers.routes.javascript.Exports.craCustomer(),
						controllers.routes.javascript.Exports.payEmployee(),
						controllers.routes.javascript.Exports.payCustomer(),

                        /* Routes for PartsTimes */
						controllers.routes.javascript.PartsTimes.addPartTime(),
						controllers.routes.javascript.PartsTimes.getPartTime(),
						controllers.routes.javascript.PartsTimes.isPartTimeEnabled(),
						controllers.routes.javascript.PartsTimes.disablePartTime(),

                        /* Routes for Accounts */
						controllers.routes.javascript.Accounts.fetchVehicle(),
						controllers.routes.javascript.Accounts.saveVehicle(),
						controllers.routes.javascript.Accounts.fetchGlobalSetting(),
						controllers.routes.javascript.Accounts.saveManager(),
						controllers.routes.javascript.Accounts.saveEmail(),
						controllers.routes.javascript.Accounts.changePassword(),
						controllers.routes.javascript.Accounts.changeStandByOption(),

                        /* Routes for Employees */
						controllers.routes.javascript.Employees.fetchMissions(),
						controllers.routes.javascript.Employees.fetchMissionsWithStandby(),
						controllers.routes.javascript.Employees.fetchFeesMission(),
						controllers.routes.javascript.Employees.all(),
						controllers.routes.javascript.Employees.fetch(),
						controllers.routes.javascript.Employees.update(),
						controllers.routes.javascript.Employees.create(),
						controllers.routes.javascript.Employees.upload(),

                        /* Routes for Role */
						controllers.routes.javascript.Roles.fetch(),

                        /* Routes for Customer */
						controllers.routes.javascript.Customers.all(),
						controllers.routes.javascript.Customers.create(),
						controllers.routes.javascript.Customers.update(),

                        /* Routes for Missions */
						controllers.routes.javascript.Missions.fetchByIds(),

                        /* Routes for Times */
						controllers.routes.javascript.Times.computeWorkingDays(),
						controllers.routes.javascript.Times.computeWorkingRecurrentDays(),
						controllers.routes.javascript.Times.isWorkingDay(),
						controllers.routes.javascript.Times.getNbOpenedDays()
				)
		);
	}
}
