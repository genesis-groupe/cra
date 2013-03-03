package controllers;

import be.objectify.deadbolt.actions.And;
import be.objectify.deadbolt.actions.Restrictions;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import security.RoleName;
import security.Secured;

/**
 * User: LPU
 * Date: 10/10/12
 */
@Security.Authenticated(Secured.class)
@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
public class Helpers extends AbstractController {
	/**
	 * Create PDF
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public static Result renderPDF(final String trigramme, final Integer year, final Integer month) {
		Result result = null;
		return result;
	}
}
