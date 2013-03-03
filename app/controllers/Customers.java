package controllers;

import be.objectify.deadbolt.actions.And;
import be.objectify.deadbolt.actions.Restrictions;
import com.github.jmkgreen.morphia.annotations.Id;
import constants.Genesis;
import constants.MissionAllowanceType;
import constants.MissionType;
import constants.Pattern;
import dto.customer.CustomerLightDTO;
import dto.mission.MissionLightDTO;
import helpers.binder.ObjectIdW;
import helpers.deserializer.DateTimeDeserializer;
import helpers.deserializer.ObjectIdDeserializer;
import helpers.formatter.JodaDateTimeFormatter;
import models.Customer;
import models.Mission;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.joda.time.DateTime;
import play.data.Form;
import play.mvc.Result;
import play.mvc.Security;
import security.RoleName;
import security.Secured;

import java.math.BigDecimal;

import static play.libs.Json.toJson;

/**
 * @author leo
 */
@Security.Authenticated(Secured.class)
public class Customers extends AbstractController {

	public static class CreateCustomerForm implements CreateForm<Customer> {

		public String code;
		public String name;
		public String finalCustomer;

		public String validate() {
			return Genesis.NAME.equals(name) ? "Nom interdit" : null;
		}

		@Override
		public Customer to() {
			Customer customer = new Customer();
			customer.code = code;
			customer.name = name;
			customer.finalCustomer = finalCustomer;
			return customer;
		}
	}

	public static class UpdateCustomerForm extends UpdateForm {

		@Id
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		public ObjectId id;

		public String name;
		public String code;
		public String finalCustomer;

		public String validate() {
			return id == null ? "L'id est obligatoire" : null;
		}

		@Override
		protected void ensureDbField() {
		}
	}

	public static class CreateMissionForm implements CreateForm<Mission> {

		public String code;

		public String label;

		public MissionType missionType;

		public MissionAllowanceType allowanceType;

		public BigDecimal distance;

		@JsonDeserialize(using = DateTimeDeserializer.class)
		@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
		public DateTime startDate = DateTime.now();

		@JsonDeserialize(using = DateTimeDeserializer.class)
		@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
		public DateTime endDate;

		public String validate() {
			return null;
		}

		@Override
		public Mission to() {
			final Mission mission = new Mission();
			mission.code = this.code;
			mission.label = this.label;
			mission.missionType = this.missionType;
			mission.allowanceType = this.allowanceType;
			mission.distance = this.distance;
			mission.startDate = this.startDate;
			mission.endDate = this.endDate;
			return mission;
		}
	}

	@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR), @And(RoleName.COLLABORATOR)})
	public static Result all() {
		return ok(toJson(CustomerLightDTO.of(Customer.all())));
	}

	@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR)})
	public static Result fetch(final ObjectIdW id) {
		return ok(toJson(CustomerLightDTO.of(Customer.findById(id.value))));
	}

	@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR)})
	public static Result create() {
		final Form<CreateCustomerForm> form = form(CreateCustomerForm.class).bind(request().body().asJson());
		if (form.hasErrors()) {
			return badRequest(form.errorsAsJson());
		}
		final CreateCustomerForm createCustomerForm = form.get();
		return ok(toJson(CustomerLightDTO.of(createCustomerForm.to().<Customer>insert())));
	}

	@Restrictions({@And(RoleName.GESTION), @And(RoleName.ADMINISTRATOR)})
	public static Result update(final ObjectIdW id) throws IllegalAccessException {
		final Form<UpdateCustomerForm> form = form(UpdateCustomerForm.class).bind(request().body().asJson());
		if (form.hasErrors()) {
			return badRequest(form.errorsAsJson());
		}
		final UpdateCustomerForm updateCustomerForm = form.get();
		return ok(toJson(CustomerLightDTO.of(Customer.updateFields(id.value, updateCustomerForm.fields()))));
	}

	public static Result addMission(final ObjectIdW id) {
		final Form<CreateMissionForm> form = form(CreateMissionForm.class).bind(request().body().asJson());
		if (form.hasErrors()) {
			return badRequest(form.errorsAsJson());
		}
		final Mission mission = form.get().to();
		mission.customerId = id.value;
		return ok(toJson(CustomerLightDTO.MissionDTO.of(mission.<Mission>insert())));
	}

	public static Result fetchMissions(final ObjectIdW id) {
		return ok(toJson(MissionLightDTO.of(Mission.findByCustomer(id.value))));
	}
}
