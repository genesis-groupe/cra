package models;

import com.github.jmkgreen.morphia.annotations.*;
import com.google.common.collect.Lists;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author f.patin
 */
@Embedded
public class MissionCounter {

	@Reference
	public Mission mission;

	@Transient
	public BigDecimal nbDays = BigDecimal.ZERO;
	private String _nbDaysAsMillis = "0";

	@SuppressWarnings({"unused"})
	@PrePersist
	private void prePersist() {
		if (this.nbDays != null) {
			_nbDaysAsMillis = this.nbDays.toPlainString();
		}
	}

	@SuppressWarnings({"unused"})
	@PostLoad
	private void postLoad() {
		if (_nbDaysAsMillis != null) {
			this.nbDays = new BigDecimal(_nbDaysAsMillis);
		}
	}

	@SuppressWarnings({"unused"})
	public MissionCounter() {
	}

	public MissionCounter(Mission mission, BigDecimal nbDays) {
		this.mission = mission;
		this.nbDays = nbDays;
	}

	public static void merge(Map<ObjectId, List<MissionCounter>> from, Map<ObjectId, List<MissionCounter>> into) {
		for (ObjectId missionId : from.keySet()) {
			if (!into.containsKey(missionId)) {
				into.put(missionId, new ArrayList<MissionCounter>());
			}
			into.get(missionId).addAll(from.get(missionId));
		}
	}

	public static List<MissionCounter> flatten(Map<ObjectId, List<MissionCounter>> counters) {
		List<MissionCounter> missionCounters = Lists.newArrayList();

		for (ObjectId id : counters.keySet()) {
			MissionCounter mc = new MissionCounter(counters.get(id).get(0).mission, BigDecimal.ZERO);
			for (MissionCounter m : counters.get(id)) {
				mc.nbDays = mc.nbDays.add(m.nbDays);
			}
			missionCounters.add(mc);
		}
		return missionCounters;

	}
}
