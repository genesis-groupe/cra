package constants;

/**
 * User: LPU
 * Date: 15/11/12
 */
public class Vehicles {
	public enum VehicleType {
		CAR("Voiture"),
		MOTORCYCLE("Moto");

		public final String label;

		private VehicleType(String label) {
			this.label = label;
		}
	}

	public enum Brand {
		ALFA_RAMEO("Alfa Romeo"),
		AUDI("Audi"),
		BMW("BMW"),
		CHRYSLER("Chrysler"),
		CITROËN("Citroën"),
		FERRARI("Ferrari"),
		FIAT("Fiat"),
		FORD("Ford"),
		HONDA("Honda"),
		HYUNDAI("Hyundai"),
		JAGUAR("Jaguar"),
		LADA("Lada"),
		LAMBORGHINI("Lamborghini"),
		LANCIA("Lancia"),
		LAND_ROVER("Land Rover"),
		MASERATI("Maserati"),
		MAZDA("Mazda"),
		MERCEDES("Mercedes"),
		MITSUBISHI("Mitsubishi"),
		NISSAN("Nissan"),
		OPEL("Opel"),
		PEUGEOT("Peugeot"),
		PORSCHE("Porsche"),
		RENAULT("Renault"),
		ROLLS_ROYCE("Rolls Royce"),
		ROVER("Rover"),
		SAAB("Saab"),
		SEAT("Seat"),
		SKODA("Skoda"),
		SUBARU("Subara"),
		SUZUKI("Suzuki"),
		TOYOTA("Toyota"),
		VAUXHALL("Vauxhall"),
		VOLKSWAGEN("Volkswagen"),
		VOLVO("Volvo");

		public final String code;
		public final String label;

		private Brand(String label) {
			this.code = this.name();
			this.label = label;
		}
	}
}
