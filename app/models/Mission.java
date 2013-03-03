package models;

import com.github.jmkgreen.morphia.annotations.*;
import com.github.jmkgreen.morphia.mapping.Mapper;
import com.github.jmkgreen.morphia.query.Query;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import constants.Genesis;
import constants.MissionAllowanceType;
import constants.MissionType;
import constants.Pattern;
import helpers.deserializer.DateTimeDeserializer;
import helpers.deserializer.ObjectIdDeserializer;
import helpers.formatter.JodaDateTimeFormatter;
import helpers.serializer.DateTimeSerializer;
import helpers.serializer.ObjectIdSerializer;
import leodagdag.play2morphia.Model;
import leodagdag.play2morphia.MorphiaPlugin;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;
import play.Logger;
import play.libs.F;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * User: leo
 * Date: 09/10/12
 * Time: 20:39
 */
@Entity
public class Mission extends AbstractCrud {

	@Id
	@JsonSerialize(using = ObjectIdSerializer.class)
	@JsonDeserialize(using = ObjectIdDeserializer.class)
	public ObjectId id;

	public String code;

	public String label;

	@Transient
	public MissionType missionType = MissionType.CUSTOMER;
	private String _missionType = MissionType.CUSTOMER.code;

	@Transient
	public MissionAllowanceType allowanceType = MissionAllowanceType.NONE;
	private String _allowanceType = MissionAllowanceType.NONE.code;

	@Transient
	public BigDecimal distance = BigDecimal.ZERO;
	private String _distance = "0";

	@JsonSerialize(using = ObjectIdSerializer.class)
	@JsonDeserialize(using = ObjectIdDeserializer.class)
	public ObjectId customerId;

	@Transient
	public String customerName;

	@Transient
	public String customerMissionName;

	@Transient
	public Boolean isGenesis;

	@Transient
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime startDate;
	private Date _startDate;

	@Transient
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime endDate;
	private Date _endDate;

	public List<StandByMoment> standByMoments = Lists.newArrayList();

	@SuppressWarnings({"unused"})
	@PrePersist
	private void prePersist() {
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

	@SuppressWarnings({"unused"})
	@PostLoad
	private void postLoad() {
		if (_startDate != null) {
			startDate = new DateTime(_startDate.getTime());
		}
		if (_endDate != null) {
			endDate = new DateTime(_endDate.getTime());
		}
		if (_missionType != null) {
			missionType = MissionType.valueOf(_missionType);
		}
		if (_allowanceType != null) {
			allowanceType = MissionAllowanceType.valueOf(_allowanceType);
		}
		if (customerId != null) {
			Customer customer = Customer.getOnlyFields(customerId, "name", "isGenesis");
			if (customer == null) {
				Logger.error(String.format("Lien Mission <-> Client cassé : %s", this.toString()));
			} else {
				customerName = customer.name;
				customerMissionName = formatCustomerMissionName();
				isGenesis = customer.isGenesis;
			}
		}
		if (_distance != null) {
			this.distance = new BigDecimal(_distance);
		}
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Mission)) return false;

		Mission other = (Mission) obj;

		return Objects.equal(id, other.id)
				&& Objects.equal(code, other.code)
				&& Objects.equal(label, other.label)
				&& Objects.equal(customerId, other.customerId)
				&& Objects.equal(startDate, other.startDate)
				&& Objects.equal(endDate, other.endDate);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(id, code, label, label, customerId, startDate, endDate);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)/**/
				.add("id", id)/**/
				.add("code", code)/**/
				.add("label", label)/**/
				.add("_missionType", _missionType)/**/
				.add("_allowanceType", _allowanceType)/**/
				.add("_distance", _distance)/**/
				.add("customerId", customerId)/**/
				.add("customerName", customerName)/**/
				.add("customerMissionName", customerMissionName)/**/
				.add("_startDate", _startDate)/**/
				.add("_endDate", _endDate)/**/
				.toString();
	}

	private static Model.Finder<ObjectId, Mission> find() {
		return new Model.Finder<>(ObjectId.class, Mission.class);
	}

	private static Query<Mission> queryToFindMe(ObjectId id) {
		return MorphiaPlugin.ds().createQuery(Mission.class)
				.field(Mapper.ID_KEY).equal(id);
	}

	public static List<Mission> all() {
		return find().all();
	}

	public static Mission findById(ObjectId id) {
		return queryToFindMe(id).get();
	}

	public static List<Mission> findByType(MissionType missionType) {
		return find()
				.field("_missionType").equal(missionType.name())
				.asList();
	}

	public static Mission findByCode(String code) {
		return find()
				.field("code").equal(code)
				.get();
	}

	public static List<Mission> findByIds(List<ObjectId> ids) {
		return find()
				.field(Mapper.ID_KEY).in(ids)
				.asList();
	}

	public static List<Mission> getGenesisMissions() {
		return find()
				.field("customerId").equal(Customer.findByName(Genesis.NAME).id)
				.asList();
	}

	private String formatCustomerMissionName() {
		return String.format(Pattern.CUSTOMER_MISISON_NAME, this.customerName, this.code);
	}

	public static Mission updateFields(final ObjectId id, final List<F.Tuple<String, Object>> fields) {
		return updateFields(Mission.class, id, fields);
	}

	public static List<Mission> findByCustomer(final Customer customer) {
		return findByCustomer(customer.id);
	}

	public static List<Mission> findByCustomer(final ObjectId customerId) {
		return Mission.find()
				.field("customerId").equal(customerId)
				.asList();
	}

	public static List<Mission> findGenesisMission(){
		return find()
				.field("customerId").equal(Customer.Genesis().id)
				.asList();
	}
}
