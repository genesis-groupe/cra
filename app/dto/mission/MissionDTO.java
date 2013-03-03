package dto.mission;

import com.google.common.collect.Lists;
import constants.MissionAllowanceType;
import constants.MissionType;
import constants.Pattern;
import helpers.deserializer.DateTimeDeserializer;
import helpers.deserializer.ObjectIdDeserializer;
import helpers.formatter.JodaDateTimeFormatter;
import helpers.serializer.DateTimeSerializer;
import helpers.serializer.ObjectIdSerializer;
import models.Mission;
import models.StandByMoment;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author f.patin
 */
public class MissionDTO {
	@JsonSerialize(using = ObjectIdSerializer.class)
	@JsonDeserialize(using = ObjectIdDeserializer.class)
	public ObjectId id;

	public String code;

	public String label;

	public MissionType missionType = MissionType.CUSTOMER;

	public MissionAllowanceType allowanceType = MissionAllowanceType.NONE;

	public BigDecimal distance = BigDecimal.ZERO;

	@JsonSerialize(using = ObjectIdSerializer.class)
	@JsonDeserialize(using = ObjectIdDeserializer.class)
	public ObjectId customerId;

	public String customerName;

	public Boolean isGenesis;

	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime startDate;

	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime endDate;

	public List<StandByMoment> standByMoments = Lists.newArrayList();

	public MissionDTO(final Mission mission) {
		this.id = mission.id;
		this.code = mission.code;
		this.label = mission.label;
		this.missionType = mission.missionType;
		this.allowanceType = mission.allowanceType;
		this.distance = mission.distance;
		this.customerId = mission.customerId;
		this.customerName = mission.customerName;
		this.isGenesis = mission.isGenesis;
		this.startDate = mission.startDate;
		this.endDate = mission.endDate;
		this.standByMoments = mission.standByMoments;
	}

	public static MissionDTO of(final Mission mission) {
		return new MissionDTO(mission);
	}
}
