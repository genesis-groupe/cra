package models;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.google.common.collect.Maps;
import helpers.deserializer.ObjectIdDeserializer;
import helpers.serializer.ObjectIdSerializer;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author f.patin
 */
@Embedded
public class StandBy {

	public static Comparator<StandBy> BY_INDEX = new Comparator<StandBy>() {
		@Override
		public int compare(StandBy sb1, StandBy sb2) {
			return sb1.standByMomentIndex.compareTo(sb2.standByMomentIndex);
		}
	};

	@JsonSerialize(using = ObjectIdSerializer.class)
	@JsonDeserialize(using = ObjectIdDeserializer.class)
	public ObjectId missionId;

	public Integer standByMomentIndex;

	@SuppressWarnings({"unused"})
	public StandBy() {
	}

	public StandBy(final ObjectId missionId, final Integer standByMomentIndex) {
		this.missionId = missionId;
		this.standByMomentIndex = standByMomentIndex;
	}

	public static Map<Integer, List<StandBy>> filterByCustomer(final Customer customer, final Map<Integer, List<StandBy>> standBys) {
		final Map<Integer, List<StandBy>> result = Maps.newHashMap();
		final Map<ObjectId, Mission> missions = Maps.newHashMap();
		for (Integer week : standBys.keySet()) {
			for (StandBy standBy : standBys.get(week)) {
				if (!missions.containsKey(standBy.missionId)) {
					missions.put(standBy.missionId, Mission.findById(standBy.missionId));
				}
				Mission mission = missions.get(standBy.missionId);
				if (customer.id.equals(mission.customerId)) {
					if (!result.containsKey(week)) {
						result.put(week, new ArrayList<StandBy>());
					}
					result.get(week).add(standBy);
				}
			}
		}
		return result;
	}
}
