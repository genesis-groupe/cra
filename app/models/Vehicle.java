package models;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.PostLoad;
import com.github.jmkgreen.morphia.annotations.PrePersist;
import com.github.jmkgreen.morphia.annotations.Transient;
import constants.Vehicles.VehicleType;
import leodagdag.play2morphia.Model;

/**
 * @author LPU
 * @author f.patin
 *         Date: 08/11/12
 */
@Embedded
public class Vehicle extends Model {

	@Transient
	public VehicleType type;
	private String _type;

	public Integer power;

	public String brand;

	public String matriculation;

	@SuppressWarnings({"unused"})
	@PrePersist
	private void prePersist() {
		if (type != null) {
			_type = type.name();
		}
	}

	@SuppressWarnings({"unused"})
	@PostLoad
	private void postLoad() {
		if (_type != null) {
			type = VehicleType.valueOf(_type);
		}
	}

}
