package constants;

/**
 * User: leodagdag
 * Date: 02/08/12
 * Time: 14:53
 */
public enum ActorType {
	PAY("payActor"), /**/
	CRA("craActor"),
	WEEK("weekActor");

	private String internalName;

	private ActorType(String internalName) {
		this.internalName = internalName;
	}

	public String getInternalName() {
		return internalName;
	}

	public String getUniqueName() {
		return String.format("%s_%s", getInternalName(), System.currentTimeMillis());
	}
}
