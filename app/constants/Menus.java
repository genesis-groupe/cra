package constants;

/**
 * User: f.patin
 * Date: 21/08/12
 * Time: 19:46
 */
public enum Menus {
	CRA("CRA"),
	HOLIDAYS("Absences"),
	FEES("Frais de déplacement"),
	PAY("Rémunération"),
	ADMIN("Administration");

	public final String label;

	Menus(final String label) {
		this.label = label;
	}
}
