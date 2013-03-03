package constants;

import org.joda.time.DateTimeConstants;

/**
 * User: LPU
 * Date: 20/11/12
 */
public enum DaysOfWeek {
	MONDAY(DateTimeConstants.MONDAY, "lundi", "lun"),
	TUESDAY(DateTimeConstants.TUESDAY, "mardi", "mar"),
	WEDNESDAY(DateTimeConstants.WEDNESDAY, "mercredi", "mer"),
	THURSDAY(DateTimeConstants.THURSDAY, "jeudi", "jeu"),
	FRIDAY(DateTimeConstants.FRIDAY, "vendredi", "ven"),
	SATURDAY(DateTimeConstants.SATURDAY, "Samedi", "sam"),
	SUNDAY(DateTimeConstants.SUNDAY, "dimanche", "dim");

	public final Integer value;
	public final String label;
	public final String shortLabel;

	private DaysOfWeek(Integer value, String label, final String shortLabel) {
		this.value = value;
		this.label = label;
		this.shortLabel = shortLabel;
	}

	public static String getNameByValue(Integer value) {
		for (DaysOfWeek day : DaysOfWeek.values()) {
			if (day.value.equals(value)) {
				return day.label;
			}
		}
		return null;
	}

	public static String getShortLabelByValue(Integer value) {
		for (DaysOfWeek day : DaysOfWeek.values()) {
			if (day.value.equals(value)) {
				return day.shortLabel;
			}
		}
		return null;
	}
}
