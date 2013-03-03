package constants;

/**
 * User: f.patin
 * Date: 06/08/12
 * Time: 21:15
 */
public enum MissionType {
	CUSTOMER(null, "Client", true),
	PRESALES("AV", "Avant vente", true),
	INTERNAL_WORK("TI", "Travaux internes", false),
	HOLIDAY("CP", "Congé payé", false),
	UNPAID("TP", "Temps Partiel", false);

	public final String code;
	public final String label;
	public final Boolean isCharged;

	private MissionType(final String code, final String label, final Boolean isCharged) {
		this.code = code;
		this.label = label;
		this.isCharged = isCharged;
	}

	public static MissionType byName(String name) {
		for (MissionType missionType : MissionType.values()) {
			if (missionType.name().equals(name)) {
				return missionType;
			}
		}
		return null;
	}
}
