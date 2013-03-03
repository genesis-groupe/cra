package testUtils;

import com.google.common.collect.Maps;
import controllers.routes;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

/**
 * @author f.patin
 */
public class AutenticationFactory {

	public static Result doAuthentication(String username, String password) {
		Map<String, String> data = Maps.newHashMap();
		data.put("username", username);
		data.put("password", password);

        /*Result result = callAction(routes.ref.Application.authenticate(), fakeRequest().withFormUrlEncodedBody(data));*/
		Result result = callAction(routes.ref.Application.authenticate(), fakeRequest(POST, "/login").withFormUrlEncodedBody(data));

		assertThat(status(result)).isEqualTo(Http.Status.SEE_OTHER);
		assertThat(redirectLocation(result)).isEqualTo("/");
		return result;
	}
}