package security;

import cache.UserCache;
import controllers.routes;
import models.User;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;

public class Secured extends Security.Authenticator {

	/**
	 * Return the username of connected user.
	 * We check if it exists in the system.
	 *
	 * @param ctx The <code>Application</code> <code>Context</code>
	 * @return the username of connected user or null if it does not exist.
	 */
	@Override
	public String getUsername(final Http.Context ctx) {
		String username = null;
		User user = UserCache.getInstance().getByUsername(ctx.session().get("username"));
		if (user != null) {
			username = user.username;
		}
		return username;

	}

	@Override
	public Result onUnauthorized(Http.Context ctx) {
		return Results.redirect(routes.Application.login());
	}

	public User getConnectedUser(final Http.Context ctx) {
		return UserCache.getInstance().getByUsername(ctx.session().get("username"));
	}

}
