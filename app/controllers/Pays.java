package controllers;

import be.objectify.deadbolt.actions.And;
import be.objectify.deadbolt.actions.Restrictions;
import helpers.ErrorMsgFactory;
import helpers.SecurityHelper;
import helpers.binder.BigDecimalW;
import helpers.binder.ObjectIdW;
import helpers.exception.CraNotFoundException;
import helpers.time.TimerHelper;
import models.Employee;
import models.Pay;
import models.User;
import play.libs.Akka;
import play.mvc.Result;
import play.mvc.Security;
import security.RoleName;
import security.Secured;
import views.html.pays.show;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static play.libs.Json.toJson;

/**
 * @author f.patin
 */
@Security.Authenticated(Secured.class)
@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR)})
public class Pays extends AbstractCraController {

	/**
	 * Show Remunerate page
	 *
	 * @return
	 */
	public static Result show() {
		User userConnected = SecurityHelper.getConnectedUser();
		List<Employee> employees = Employee.getOnlyEmployees();
		Collections.sort(employees, Employee.BY_TRIGRAMME);
		return ok(show.render(userConnected, employees, Arrays.asList(2012, 2013), TimerHelper.MonthType.all()));
	}

	public static Result fetch(final String trigramme, final Integer year, final Integer month, final BigDecimalW hourlyRateW) {
		return async(Akka.future(new Callable<Result>() {
			@Override
			public Result call() throws Exception {
				final BigDecimal hourlyRate = hourlyRateW.value;
				try {
					return ok(toJson(Pay.getOrCompute(trigramme, year, month, hourlyRate)));
				} catch (CraNotFoundException e) {
					return badRequest(ErrorMsgFactory.asJson(String.format("Collaborateur : %s, Cra : %s %s", trigramme, TimerHelper.MonthType.toMonth(month).label, year), "Il n'y a pas de Cra saisie pour ce collaborateur"));
				}
			}
		}));
	}

	public static Result validate(final ObjectIdW id) {
		if (id.value == null) {
			return badRequest(ErrorMsgFactory.asJson("La rémunération n'existe pas"));
		}
		return ok(toJson(Pay.validate(id.value)));
	}

	public static Result invalidate(final ObjectIdW id) {
		if (id.value == null) {
			return badRequest(ErrorMsgFactory.asJson("La rémunération n'existe pas"));
		}
		return ok(toJson(Pay.invalidate(id.value)));
	}
}
