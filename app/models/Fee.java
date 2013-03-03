package models;

import cache.FeeParameterCache;
import com.github.jmkgreen.morphia.annotations.*;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import constants.FeeType;
import constants.Pattern;
import constants.Vehicles.VehicleType;
import helpers.deserializer.DateTimeDeserializer;
import helpers.deserializer.FeesTypeDeserializer;
import helpers.formatter.JodaDateTimeFormatter;
import helpers.serializer.DateTimeSerializer;
import helpers.serializer.FeesTypeSerializer;
import leodagdag.play2morphia.Model;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author f.patin
 */
@Embedded
public class Fee extends Model {

	@Transient
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime date;
	private Date _date;

	public Integer week;

	@Transient
	@JsonSerialize(using = FeesTypeSerializer.class)
	@JsonDeserialize(using = FeesTypeDeserializer.class)
	public FeeType type;
	private String _type;

	@Transient
	public BigDecimal kilometers;
	private String _kilometers;

	@Transient
	public BigDecimal kilometersAmount;
	private String _kilometersAmount;

	public String journey;

	@Transient
	public BigDecimal amount;
	private String _amount;

	public String description;

	@Transient
	public BigDecimal total;
	private String _total;

	@Reference
	public Mission mission;

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Fee)) return false;

		Fee other = (Fee) obj;

		return Objects.equal(date, other.date)
				&& Objects.equal(type, other.type)
				&& Objects.equal(kilometers, other.kilometers)
				&& Objects.equal(kilometersAmount, other.kilometersAmount)
				&& Objects.equal(journey, other.journey)
				&& Objects.equal(amount, other.amount)
				&& Objects.equal(description, other.description)
				&& Objects.equal(total, other.total);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(date, type, kilometers, kilometersAmount, journey, amount, description, total);
	}

	@SuppressWarnings({"unused"})
	@PrePersist
	private void prePersist() {
		if (this.date != null) {
			_date = this.date.toDate();
			week = date.getWeekOfWeekyear();
		}
		if (this.amount != null) {
			_amount = this.amount.toPlainString();
		}
		if (this.type != null) {
			_type = this.type.name();
		}
		if (this.kilometers != null) {
			_kilometers = this.kilometers.toPlainString();
		}
		if (this.kilometersAmount != null) {
			_kilometersAmount = this.kilometersAmount.toPlainString();
		}
		if (this.total != null) {
			_total = this.total.toPlainString();
		}
	}

	@SuppressWarnings({"unused"})
	@PostLoad
	private void postLoad() {
		if (_date != null) {
			date = new DateTime(_date.getTime());
			week = date.getWeekOfWeekyear();
		}
		if (_amount != null) {
			this.amount = new BigDecimal(_amount);
		}
		if (_type != null) {
			this.type = FeeType.byName(_type);
		}
		if (_kilometers != null) {
			this.kilometers = new BigDecimal(_kilometers);
		}
		if (_kilometersAmount != null) {
			this.kilometersAmount = new BigDecimal(_kilometersAmount);
		}
		if (_total != null) {
			this.total = new BigDecimal(_total);
		}
	}

	@SuppressWarnings({"unused"})
	public Fee() {
	}

	public Fee(DateTime date, FeeType type, Mission mission, BigDecimal amount) {
		this.date = date;
		this.week = date.getWeekOfWeekyear();
		this.type = type;
		this.mission = mission;
		this.amount = amount;
		this.total = amount;
	}

	public static List<Fee> fetch(final String trigramme, final Integer year, final Integer month) {
		final List<Fee> fees = Cra.computeFees(trigramme, year, month, Boolean.FALSE);
		Iterables.removeIf(fees, new Predicate<Fee>() {
			@Override
			public boolean apply(@Nullable Fee fee) {
				return FeeType.MISSION_ALLOWANCE.equals(fee.type);
			}
		});
		return fees;
	}

	public void compute(final String trigramme) {
		BigDecimal amount = this.amount != null ? this.amount : BigDecimal.ZERO;
		kilometersAmount = BigDecimal.ZERO;
		if (kilometers != null) {
			Employee employee = Employee.findByTrigramme(trigramme);
			if (employee.vehicle != null) {
				BigDecimal ref = BigDecimal.ZERO;
				if (VehicleType.CAR.equals(employee.vehicle.type)) {
					if (employee.vehicle.power >= 0 && employee.vehicle.power < 5) {
						ref = FeeParameterCache.get(this.date, this.date).cars.get(0);
					} else if (employee.vehicle.power >= 5 && employee.vehicle.power < 8) {
						ref = FeeParameterCache.get(this.date, this.date).cars.get(5);
					} else if (employee.vehicle.power >= 8 && employee.vehicle.power < 11) {
						ref = FeeParameterCache.get(this.date, this.date).cars.get(8);
					} else if (employee.vehicle.power >= 11) {
						ref = FeeParameterCache.get(this.date, this.date).cars.get(11);
					}
				} else {
					if (employee.vehicle.power < 501) {
						ref = FeeParameterCache.get(this.date, this.date).motorcycles.get(0);
					} else if (employee.vehicle.power >= 501) {
						ref = FeeParameterCache.get(this.date, this.date).motorcycles.get(501);
					}
				}
				kilometersAmount = kilometers.multiply(ref);
			}
		}
		total = amount.add(kilometersAmount);
	}

}
