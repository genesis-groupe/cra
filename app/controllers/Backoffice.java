package controllers;

import be.objectify.deadbolt.actions.And;
import be.objectify.deadbolt.actions.Restrictions;
import helpers.SecurityHelper;
import play.mvc.Result;
import play.mvc.Security;
import security.RoleName;
import security.Secured;
import views.html.backoffice.show;

/**
 * @author f.patin
 */
@Security.Authenticated(Secured.class)
@Restrictions({@And(RoleName.ADMINISTRATOR)})
public class Backoffice extends AbstractController {
	public static Result show() {
		return ok(show.render(SecurityHelper.getConnectedUser()));
	}
}
