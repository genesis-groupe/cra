package security;

import be.objectify.deadbolt.AbstractDeadboltHandler;
import be.objectify.deadbolt.DynamicResourceHandler;
import be.objectify.deadbolt.models.RoleHolder;
import cache.UserCache;
import play.mvc.Http;
import play.mvc.Result;

/**
 * User: leodagdag
 * Date: 10/07/12
 * Time: 11:18
 * To change this template use File | Settings | File Templates.
 */
public class MyDeadboltHandler extends AbstractDeadboltHandler {

	@Override
	public Result beforeRoleCheck(Http.Context context) {
		// returning null means that everything is OK.  Return a real result if you want a redirect to a login page or
		// somewhere else
		return null;
	}

	@Override
	public RoleHolder getRoleHolder(Http.Context context) {
		String username = context.session().get("username");
		RoleHolder roleHolder = null;
		if (username != null) {
			roleHolder = UserCache.getInstance().getByUsername(username);
			if (roleHolder == null) {
				context.request().setUsername(null);
			}
		}
		return roleHolder;
	}

	@Override
	public DynamicResourceHandler getDynamicResourceHandler(Http.Context context) {
		return null;
	}

	@Override
	public Result onAccessFailure(Http.Context context, String content) {
		return forbidden(content);
	}
}
