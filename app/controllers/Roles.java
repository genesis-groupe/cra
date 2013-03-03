package controllers;

import be.objectify.deadbolt.actions.And;
import be.objectify.deadbolt.actions.Restrictions;
import helpers.binder.ObjectIdW;
import models.Role;
import play.mvc.Result;
import play.mvc.Security;
import security.RoleName;
import security.Secured;

import static play.libs.Json.toJson;

/**
 * @author f.patin
 */
@Security.Authenticated(Secured.class)
@Restrictions({@And(RoleName.ADMINISTRATOR)})
public class Roles extends AbstractController {

	public static Result all() {
		return ok(toJson(Role.all()));
	}

	public static Result fetch(ObjectIdW id) {
		return ok(toJson(Role.findbyId(id.value)));
	}
}
