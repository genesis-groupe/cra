package controllers;

import be.objectify.deadbolt.actions.And;
import be.objectify.deadbolt.actions.Restrictions;
import com.google.common.collect.Lists;
import helpers.ErrorMsgFactory;
import helpers.SecurityHelper;
import helpers.binder.ObjectIdW;
import helpers.deserializer.ObjectIdDeserializer;
import helpers.serializer.ObjectIdSerializer;
import helpers.time.TimerHelper;
import models.*;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;
import play.data.Form;
import play.libs.Akka;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import security.RoleName;
import security.Secured;
import views.html.cras.show;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import static play.libs.Json.toJson;

/**
 * @author f.patin
 */
@Security.Authenticated(Secured.class)
@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
public class Cras extends AbstractCraController {

	public static class CommentForm extends AbstractCraForm {

		public String comment;

		public String validate() {
			return id == null ? "Le CRA n'existe pas" : null;
		}
	}

	public static class StandBysForm extends AbstractCraForm {

		public List<StandBy> standBy;

		public Integer week;

		public String validate() {
			return trigramme == null || year == null || month == null ? "Paramètres invalides" : null;
		}
	}

	public static class StandByForm {
		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		public ObjectId missionId;

		public List<Integer> standByMomentIndex;

	}

	/**
	 * Show Create Cra page
	 */
	public static Result show() {
		User user = SecurityHelper.getConnectedUser();
		final List<Employee> employees = loadEmployees(user.role.getRoleName());
		//TODO gestion des années à afficher
		return ok(show.render(user, employees, Arrays.asList(2012, 2013), TimerHelper.MonthType.all(), DateTime.now().getYear(), DateTime.now().getMonthOfYear()));
	}

	/**
	 * Fetch CRA
	 *
	 * @param trigramme trigramme of the <code>Employee</code>
	 * @param year      year
	 * @param month     month
	 * @return a <code>Cra</code>
	 */
	public static Result fetch(final String trigramme, final Integer year, final Integer month) {
		return async(Akka.future(new Callable<Result>() {
			@Override
			public Result call() throws Exception {
				return ok(fetchCra(trigramme, year, month, Boolean.TRUE));
			}
		}));
	}

	public static Result fetchFees(final String trigramme, final Integer year, final Integer month) {
		return async(Akka.future(new Callable<Result>() {
			@Override
			public Result call() throws Exception {
				return ok(toJson(Cra.computeFees(trigramme, year, month, Boolean.TRUE)));
			}
		}));
	}

	public static Result fetchCustomers(final String trigramme, final Integer year, final Integer month) {
		return ok(toJson(Cra.fetchCustomers(trigramme, year, month)));
	}

	/**
	 * Sev Cra
	 * We delete all existing days
	 * And we create new ones.
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public static Result save() {

		final Form<Cra> form = form(Cra.class).bind(request().body().asJson());
		if (form.hasErrors()) {
			return badRequest(form.errorsAsJson());
		}
		return async(Akka.future(new Callable<Result>() {
			@Override
			public Result call() throws Exception {
				final String trigramme = form.get().employee.trigramme;
				final Integer year = form.get().year;
				final Integer month = form.get().month.value;
				final ObjectId idCra = form.get().id;
				final List<Day> days = form.get().days;

				final Cra cra;
				// Mise à jour du Cra
				if (idCra != null) {
					cra = Cra.findById(idCra);
				} else {
					cra = Cra.create(trigramme, year, month);
				}
				cra.updateDays(days);

				return ok(fetchCra(trigramme, year, month, Boolean.TRUE));
			}
		}));
	}

	public static Result deleteDay(final String trigramme, final Integer year, final Integer month, final Integer day) {
		return async(Akka.future(new Callable<Result>() {
			@Override
			public Result call() throws Exception {
				Cra.deleteDay(trigramme, year, month, new DateTime(year, month, day, 0, 0));
				return ok(fetchCra(trigramme, year, month, true));
			}
		}));
	}

	@BodyParser.Of(BodyParser.Json.class)
	public static Result saveStandBys() {

		final Form<StandBysForm> form = form(StandBysForm.class).bind(request().body().asJson());
		if (form.hasErrors()) {
			return badRequest(form.errorsAsJson());
		} else {
			return async(Akka.future(new Callable<Result>() {
				@Override
				public Result call() throws Exception {
					final String trigramme = form.get().trigramme;
					final Integer year = form.get().year;
					final Integer month = form.get().month;
					final Integer week = form.get().week;
					final List<StandBy> standBy = form.get().standBy;
					Cra.saveStandBy(trigramme, year, month, week, standBy);
					return ok(fetchCra(trigramme, year, month, Boolean.TRUE));
				}
			}));
		}
	}

	@BodyParser.Of(BodyParser.Json.class)
	public static Result saveComment() {
		final Form<CommentForm> form = form(CommentForm.class).bind(request().body().asJson());
		if (form.hasErrors()) {
			return badRequest(form.errorsAsJson());
		} else {
			return async(Akka.future(new Callable<Result>() {
				@Override
				public Result call() throws Exception {
					final ObjectId id = form.get().id;
					final String comment = form.get().comment;
					Cra.saveComment(id, comment);
					return ok();
				}
			}));
		}
	}

	@BodyParser.Of(BodyParser.Json.class)
	public static Result deleteComment(final ObjectIdW id) {
		return async(Akka.future(new Callable<Result>() {
			@Override
			public Result call() throws Exception {
				Cra.deleteComment(id.value);
				return ok();
			}
		}));
	}

	public static Result validate(final ObjectIdW id) {
		if (id.value == null) {
			return badRequest(ErrorMsgFactory.asJson("Le CRA n'existe pas"));
		}
		return async(Akka.future(new Callable<Result>() {
			@Override
			public Result call() throws Exception {
				Cra.validate(id.value);
				return ok();
			}
		}));
	}

	public static Result invalidate(final ObjectIdW id) {
		if (id.value == null) {
			return badRequest(ErrorMsgFactory.asJson("Le CRA n'existe pas"));
		}
		return async(Akka.future(new Callable<Result>() {
			@Override
			public Result call() throws Exception {
				Cra.invalidate(id.value);
				return ok();
			}
		}));
	}

}
