package controllers;

import be.objectify.deadbolt.actions.And;
import be.objectify.deadbolt.actions.Restrictions;
import com.github.jmkgreen.morphia.annotations.Transient;
import com.google.common.collect.Iterables;
import constants.MissionAllowanceType;
import constants.MissionType;
import constants.Pattern;
import dto.mission.MissionDTO;
import helpers.binder.ObjectIdW;
import helpers.deserializer.DateTimeDeserializer;
import helpers.formatter.JodaDateTimeFormatter;
import models.Mission;
import models.StandByMoment;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.joda.time.DateTime;
import play.data.Form;
import play.mvc.Result;
import play.mvc.Security;
import security.RoleName;
import security.Secured;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static play.libs.Json.toJson;

/**
 * @author f.patin
 */
@Security.Authenticated(Secured.class)
@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
public class Missions extends AbstractController {

	public static class MissionsForm {

		public List<ObjectId> ids;

		public String validate() {
			return ids == null || Iterables.isEmpty(ids) ? "Paramètres invalides" : null;
		}
	}

	public static class UpdateMissionForm extends UpdateForm {
		public String code;

		public String label;

		@Transient
		public MissionType missionType;
		private String _missionType;

		@Transient
		public MissionAllowanceType allowanceType;
		private String _allowanceType;

		@Transient
		public BigDecimal distance;
		public String _distance;

		@JsonDeserialize(using = DateTimeDeserializer.class)
		@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
		@Transient
		public DateTime startDate;
		public Date _startDate;

		@JsonDeserialize(using = DateTimeDeserializer.class)
		@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
		@Transient
		public DateTime endDate;
		public Date _endDate;

		public List<StandByMoment> standByMoments;

		public String validate() {
			return null;
		}

		@Override
		protected void ensureDbField() {
			if (startDate != null) {
				_startDate = startDate.toDate();
			}
			if (endDate != null) {
				_endDate = endDate.toDate();
			}
			if (missionType != null) {
				_missionType = missionType.name();
			}
			if (allowanceType != null) {
				_allowanceType = allowanceType.name();
			}
			if (distance != null) {
				_distance = this.distance.toPlainString();
			}
		}
	}

	public static Result fetch(final ObjectIdW id) {
		return ok(toJson(MissionDTO.of(Mission.findById(id.value))));
	}

	public static Result update(final ObjectIdW id) throws IllegalAccessException {
		final Form<UpdateMissionForm> form = form(UpdateMissionForm.class).bind(request().body().asJson());
		if (form.hasErrors()) {
			return badRequest(form.errorsAsJson());
		}
		final UpdateMissionForm updateMissionForm = form.get();

		return ok(toJson(MissionDTO.of(Mission.updateFields(id.value, updateMissionForm.fields()))));
	}

	public static Result fetchByIds() {
		final Form<MissionsForm> form = form(MissionsForm.class).bind(request().body().asJson());
		if (form.hasErrors()) {
			return badRequest(form.errorsAsJson());
		}
		return ok(toJson(Mission.findByIds(form.get().ids)));
	}
}
