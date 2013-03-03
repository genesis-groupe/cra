package models;

/**
 * @author f.patin
 */
public class StandByMoment {

	public Integer index;

	public String label;

	@SuppressWarnings({"unused"})
	public StandByMoment() {
	}

	public StandByMoment(final Integer index, final String label) {
		this.index = index;
		this.label = label;
	}
}
