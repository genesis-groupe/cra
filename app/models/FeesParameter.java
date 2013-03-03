package models;

import com.github.jmkgreen.morphia.annotations.*;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import constants.MissionAllowanceType;
import constants.Pattern;
import constants.Vehicles;
import helpers.deserializer.DateTimeDeserializer;
import helpers.deserializer.ObjectIdDeserializer;
import helpers.formatter.JodaDateTimeFormatter;
import helpers.serializer.DateTimeSerializer;
import helpers.serializer.ObjectIdSerializer;
import leodagdag.play2morphia.Model;
import leodagdag.play2morphia.MorphiaPlugin;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author LPU
 * @author f.patin
 */
@Entity
public class FeesParameter extends Model {
	@Id
	@JsonSerialize(using = ObjectIdSerializer.class)
	@JsonDeserialize(using = ObjectIdDeserializer.class)
	public ObjectId id;

	@Transient
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime startDate;
	private Date _startDate;

	@Transient
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime endDate;
	private Date _endDate;

	@Embedded
	public List<FeeAllowance> feeAllowances = Lists.newArrayList();

	/**
	 * Pour cars et motorcycles
	 * <p/>
	 * key = puissance fiscale / cylindrée
	 * value = remboursement/Km
	 */
	@Transient
	public Map<Integer, BigDecimal> cars = Maps.newHashMap();
	private Map<Integer, String> _cars = Maps.newHashMap();

	@Transient
	public Map<Integer, BigDecimal> motorcycles = Maps.newHashMap();
	private Map<Integer, String> _motorcycles = Maps.newHashMap();

	/**
	 * key = département
	 * value = remboursement/Km
	 */
	@Transient
	public Map<Integer, BigDecimal> farTravels = Maps.newHashMap();
	private Map<Integer, String> _farTravels = Maps.newHashMap();

	@Transient
	public BigDecimal provinceTravel;
	private String _provinceTravel;

	@SuppressWarnings({"unused"})
	@PrePersist
	private void prePersist() {
		if (startDate != null) {
			_startDate = startDate.toDate();
		}
		if (endDate != null) {
			_endDate = endDate.toDate();
		}
		if (this.provinceTravel != null) {
			_provinceTravel = this.provinceTravel.toPlainString();
		}
		if (this.cars != null) {
			_cars = Maps.transformValues(this.cars, new Function<BigDecimal, String>() {
				@Override
				public String apply(@Nullable BigDecimal bigDecimal) {
					return bigDecimal.toPlainString();
				}
			});
		}
		if (this.motorcycles != null) {
			_motorcycles = Maps.transformValues(this.motorcycles, new Function<BigDecimal, String>() {
				@Override
				public String apply(@Nullable BigDecimal bigDecimal) {
					return bigDecimal.toPlainString();
				}
			});
		}
		if (this.farTravels != null) {
			_farTravels = Maps.transformValues(this.farTravels, new Function<BigDecimal, String>() {
				@Override
				public String apply(@Nullable BigDecimal bigDecimal) {
					return bigDecimal.toPlainString();
				}
			});
		}
	}

	@SuppressWarnings({"unused"})
	@PostLoad
	private void postLoad() {
		if (_startDate != null) {
			startDate = new DateTime(_startDate.getTime());
		}
		if (_endDate != null) {
			endDate = new DateTime(_endDate.getTime());
		}
		if (_provinceTravel != null) {
			this.provinceTravel = new BigDecimal(_provinceTravel);
		}
		if (_cars != null) {
			this.cars = Maps.transformValues(_cars, new Function<String, BigDecimal>() {
				@Override
				public BigDecimal apply(@Nullable String s) {
					return new BigDecimal(s);
				}
			});
		}
		if (_motorcycles != null) {
			this.motorcycles = Maps.transformValues(_motorcycles, new Function<String, BigDecimal>() {
				@Override
				public BigDecimal apply(@Nullable String s) {
					return new BigDecimal(s);
				}
			});
		}
		if (_farTravels != null) {
			this.farTravels = Maps.transformValues(_farTravels, new Function<String, BigDecimal>() {
				@Override
				public BigDecimal apply(@Nullable String s) {
					return new BigDecimal(s);
				}
			});
		}
	}

	public static FeesParameter find(final DateTime start, final DateTime end) {
		return MorphiaPlugin.ds().createQuery(FeesParameter.class)
				.field("_startDate").lessThanOrEq(start.toDate())
				.field("_endDate").greaterThanOrEq(end.toDate())
				.get();
	}

	public BigDecimal getFeeAllowanceAmount(final MissionAllowanceType missionAllowanceType) {
		for (FeeAllowance feeAllowance : this.feeAllowances) {
			if (feeAllowance.missionAllowanceType.equals(missionAllowanceType)) {
				return feeAllowance.amount;
			}
		}
		return BigDecimal.ZERO;
	}

	public BigDecimal getVehiculeReimbursementRate(final Vehicles.VehicleType vehicleType, final Integer power) {
		switch (vehicleType) {
			case CAR:
				if (power >= 0 && power < 5) {
					return this.cars.get(0);
				} else if (power >= 5 && power < 8) {
					return this.cars.get(5);
				} else if (power >= 8 && power < 11) {
					return this.cars.get(8);
				} else {
					return this.cars.get(11);
				}
			case MOTORCYCLE:
				if (power < 501) {
					return this.motorcycles.get(0);
				} else {
					return this.motorcycles.get(501);
				}
			default:
				return BigDecimal.ZERO;
		}
	}
}
