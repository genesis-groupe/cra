package constants;

/**
 * User: LPU
 * Date: 08/11/12
 */
public enum MissionCode {
	AV("Avant vente"),
	TI("Travaux internes"),
	IC("Inter-contrat"),
	F("Formation"),
	CP("Congé payé"),
	CSS("Congés Sans Solde"),
	AE("Absences Exceptionnelles"),
	MM("Maladie Maternité"),
	RECUP("Récupération"),
	RTTS("RTT Salarié"),
	RTTE("RTT Employeur"),
	TP("Temps Partiel");

	public final String code;
	public final String label;

	private MissionCode(String label) {
		this.code = this.name();
		this.label = label;
	}
}
