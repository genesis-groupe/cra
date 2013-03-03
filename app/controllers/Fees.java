package controllers;

import be.objectify.deadbolt.actions.And;
import be.objectify.deadbolt.actions.Restrictions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import constants.FeeType;
import constants.Pattern;
import helpers.ErrorMsgFactory;
import helpers.SecurityHelper;
import helpers.deserializer.DateTimeDeserializer;
import helpers.deserializer.FeesTypeDeserializer;
import helpers.deserializer.ObjectIdDeserializer;
import helpers.formatter.JodaDateTimeFormatter;
import helpers.serializer.ObjectIdSerializer;
import helpers.time.TimerHelper;
import models.*;
import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.joda.time.DateTime;
import play.api.templates.Html;
import play.data.Form;
import play.mvc.BodyParser;
import play.mvc.Result;
import play.mvc.Security;
import security.RoleName;
import security.Secured;
import views.html.fees.show;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static play.libs.Json.toJson;

/**
 * User: LPU
 * Date: 07/11/12
 */
@Security.Authenticated(Secured.class)
@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
public class Fees extends AbstractController {

	public static class FeeForm {

		public String trigramme;

		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		public ObjectId missionId;

		@JsonDeserialize(using = DateTimeDeserializer.class)
		@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
		public DateTime date;

		public BigDecimal kilometers;

		public String journey;

		@JsonDeserialize(using = FeesTypeDeserializer.class)
		public FeeType type;

		public BigDecimal amount;

		public String description;

		public String validate() {
			return null;
		}

		public List<Fee> extract() {
			List<Fee> fees = Lists.newArrayList();
			if (kilometers != null && kilometers.compareTo(BigDecimal.ZERO) > 0) {
				Fee fee = new Fee();
				fee.date = date;
				fee.mission = Mission.findById(missionId);
				fee.type = FeeType.KILOMETER;
				fee.kilometers = kilometers;
				fee.journey = journey;
				fee.description = description;
				fees.add(fee);
			}
			if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
				Fee fee = new Fee();
				fee.date = date;
				fee.mission = Mission.findById(missionId);
				fee.type = type;
				fee.amount = amount;
				fee.description = description;
				fees.add(fee);
			}
			return fees;
		}

	}

	public static class FeesDeleteForm {
		public String trigramme;

		@JsonProperty
		public Fee fee;

		public String validate() {
			return null;
		}
	}

	/**
	 * Show fees page
	 */
	public static Result show() {
		final User userConnected = SecurityHelper.getConnectedUser();

		final Employee collaborateur = SecurityHelper.findEmployeeByConnectedUser();
		final List<AffectedMission> missions = Lists.newArrayList();
		if (collaborateur != null) {
			missions.addAll(Employee.fetchFeesMission(collaborateur.trigramme));
		}

		final List<FeeType> feesType = Lists.newArrayList( Iterables.filter(Arrays.asList(FeeType.values()), new Predicate<FeeType>() {
			@Override
			public boolean apply(@Nullable FeeType feesType) {
				return !FeeType.MISSION_ALLOWANCE.equals(feesType) && !FeeType.KILOMETER.equals(feesType);
			}
		}));

		final DateTime now = DateTime.now();
		return ok(show.render(loadEmployees(userConnected.role.getRoleName()), userConnected, feesType, Arrays.asList(2012, 2013),/**/
				TimerHelper.MonthType.all(), now.getYear(), now.getMonthOfYear(), /**/
				missions));
	}

	/**
	 * Create fees
	 */
	public static Result create() {
		final Form<FeeForm> form = form(FeeForm.class).bind(request().body().asJson());
		if (form.hasErrors()) {
			return badRequest();
		}
		final String trigramme = form.get().trigramme;
		final DateTime date = form.get().date;
		final Cra cra = Cra.getOrCreate(trigramme, date.getYear(), date.getMonthOfYear());
		if (Boolean.TRUE.equals(cra.isValidated)) {
			return badRequest(ErrorMsgFactory.asJson("Vous ne pouvez pas saisir de frais pour ce mois.", "Le CRA est déjà validé"));
		}
		final List<Fee> fees = form.get().extract();
		for (Fee fee : fees) {
			fee.compute(trigramme);
		}
		cra.fees.addAll(fees);
		cra.update();
		return ok();
	}

	public static Result fetch(final String trigramme, final Integer year, final Integer month) {
		List<Fee> resultFees = Fee.fetch(trigramme, year, month);
		return ok(resultFees == null ? JsonNodeFactory.instance.nullNode() : toJson(resultFees));
	}

	/**
	 * DeleteFees
	 */
	@BodyParser.Of(BodyParser.Json.class)
	public static Result delete() {
		final Form<FeesDeleteForm> form = form(FeesDeleteForm.class).bind(request().body().asJson());
		if (form.hasErrors()) {
			return badRequest(form.errorsAsJson());
		}
		final String trigramme = form.get().trigramme;
		final Fee fee = form.get().fee;
		Cra.delete(trigramme, fee);
		return ok();
	}

	public static Result fetchType() {
		return ok(toJson(FeeType.toMap()));
	}
}
