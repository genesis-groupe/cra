package dto.mission;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import helpers.serializer.DateTimeSerializer;
import helpers.serializer.ObjectIdSerializer;
import models.Mission;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author f.patin
 */
public class MissionLightDTO {

	@JsonSerialize(using = ObjectIdSerializer.class)
	public final ObjectId id;
	public final String code;
	@JsonSerialize(using = DateTimeSerializer.class)
	public final DateTime startDate;
	@JsonSerialize(using = DateTimeSerializer.class)
	public final DateTime endDate;
	@JsonSerialize(using = ObjectIdSerializer.class)
	public final ObjectId customerId;
	public final String customerName;
	public final Boolean isGenesis;

	public MissionLightDTO(final Mission mission) {
		this.id = mission.id;
		this.code = mission.code;
		this.startDate = mission.startDate;
		this.endDate = mission.endDate;
		this.customerId = mission.customerId;
		this.customerName = mission.customerName;
		this.isGenesis = mission.isGenesis;
	}

	public static MissionLightDTO of(final Mission mission) {
		return new MissionLightDTO(mission);
	}

	public static List<MissionLightDTO> of(final List<Mission> missions) {
		return Lists.transform(missions, new Function<Mission, MissionLightDTO>() {
			@Override
			public MissionLightDTO apply(@Nullable final Mission mission) {
				return of(mission);
			}
		});
	}
}
