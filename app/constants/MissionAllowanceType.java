package constants;

/**
 * @author f.patin
 */
public enum MissionAllowanceType {
	NONE("NONE", "Aucun"),
	ZONE("ZONE", "Forfait zone"),
	REAL("REAL", "Réel"),
	CASINO("CASINO", "Forfait Casino");

	public final String code;
	public final String label;

	private MissionAllowanceType(String code, String label) {
		this.code = code;
		this.label = label;
	}
}
