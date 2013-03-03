package constants;

import com.google.common.collect.Maps;

import java.util.Comparator;
import java.util.Map;

/**
 * @author LPU
 * ATTENTION : This enuma have to be synchronised with JS Object App.FEE_TYPE in app.js
 */
public enum FeeType {
	KILOMETER(0, "Frais kilométriques"),
	PARKING(1, "Parking"),
	TOLL(2, "Péages"),
	TAXI(3, "Taxi"),
	TRAIN(4, "Train"),
	PLANE(5, "Avion"),
	PUBLIC_TRANSPORT(6, "Bus/Métro/RER"),
	TICKET(7, "Titre de transport"),
	HOTEL(8, "Hôtel/ Petit déjeuner"),
	LUNCH(9, "Déjeuner"),
	DINNER(10, "Dîner"),
	MISCELLANEOUS(11, "Frais divers"),
	VEHICLE(12, "Frais de véhicule"),
	MISSION_ALLOWANCE(13, "Mission");

	public static Comparator<FeeType> COMP = new Comparator<FeeType>() {
		@Override
		public int compare(FeeType o1, FeeType o2) {
			return o1.order.compareTo(o2.order);
		}
	};
	public final Integer order;
	public final String label;

	private FeeType(final Integer order, String label) {
		this.order = order;
		this.label = label;
	}

	public static FeeType byName(String name) {
		for (FeeType feesType : FeeType.values()) {
			if (feesType.name().equals(name)) {
				return feesType;
			}
		}
		return null;
	}

	public static Map<String, String> toMap() {
		final Map<String, String> map = Maps.newHashMap();
		for (FeeType feesType : FeeType.values()) {
			map.put(feesType.name(), feesType.label);
		}
		return map;
	}
}
