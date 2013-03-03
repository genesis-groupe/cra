import batch.BatchScheduler;
import constants.ConfigurationKey;
import constants.FeeType;
import helpers.ErrorMsgFactory;
import helpers.formatter.FeesTypeFormatter;
import helpers.formatter.JodaDateTimeFormatter;
import helpers.formatter.JodaLocalTimeFormatter;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import play.Application;
import play.Configuration;
import play.GlobalSettings;
import play.Logger;
import play.data.format.Formatters;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.lang.reflect.Method;

import static play.libs.Json.toJson;

/**
 * User: f.patin
 * Date: 10/07/12
 * Time: 10:09
 */
public class Global extends GlobalSettings {

	@Override
	public void onStart(final Application app) {
		Logger.info("Application start...");
		checkConfiguration(app);
		/*
	     * Register Formatter for Form.bindFromRequest()
         */
		Formatters.register(LocalTime.class, new JodaLocalTimeFormatter(constants.Pattern.TIME));
		Formatters.register(DateTime.class, new JodaDateTimeFormatter());
		Formatters.register(FeeType.class, new FeesTypeFormatter());

        /*
         * Launch Batches
         */
		try {
			BatchScheduler.getInstance().scheduleBatches();
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
			Logger.error("Error during schedule batches", e);
		}

		Logger.info("Application has started");
	}

	private void checkConfiguration(final Application app) {

		final Configuration root = Configuration.root();
		for (ConfigurationKey confKey : ConfigurationKey.values()) {
			Configuration conf = root.getConfig(confKey.mainKey);
			if (conf == null) {
				throw root.reportError(confKey.mainKey, String.format("missing configuration mainKey:[%s]", confKey.mainKey), null);
			}
			for (String subKey : confKey.subKey) {
				if (!conf.keys().contains(subKey)) {
					throw root.reportError(confKey.mainKey, String.format("missing configuration mainKey:[%s.%s]", confKey.mainKey, subKey), null);
				}
			}
		}

	}

	@Override
	public void onStop(final Application app) {
		Logger.info("Application shutdown...");
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Action onRequest(final Http.Request request, final Method actionMethod) {
		Logger.info(String.format("Call [%s] [%s %s]", request.remoteAddress(), request.method(), request.path()));
		return super.onRequest(request, actionMethod);
	}

	@Override
	public Result onError(Http.RequestHeader request, Throwable t) {
		Logger.error(String.format("onError [%s] [%s %s]", request.remoteAddress(), request.method(), request.path()), t);
		if (request.accept().contains("application/json")) {
			return Results.internalServerError(toJson(t));
		} else {
			return super.onError(request, t);
		}
	}

	@Override
	public Result onHandlerNotFound(Http.RequestHeader request) {
		Logger.error(String.format("onHandlerNotFound [%s] [%s %s]", request.remoteAddress(), request.method(), request.path()));
		if (request.accept().contains("application/json")) {
			return Results.notFound(ErrorMsgFactory.asJson(String.format("Not Found %s %s", request.method(), request.path())));
		} else {
			return super.onHandlerNotFound(request);
		}
	}

	@Override
	public Result onBadRequest(Http.RequestHeader request, String error) {
		Logger.error(String.format("onBadRequest [%s][%s %s] : %s", request.remoteAddress(), request.method(), request.path(), error));
		if (request.accept().contains("application/json")) {
			return Results.badRequest(toJson(error));
		} else {
			return super.onBadRequest(request, error);
		}
	}

}
