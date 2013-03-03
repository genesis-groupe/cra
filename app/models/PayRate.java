package models;

import com.github.jmkgreen.morphia.annotations.*;
import helpers.deserializer.ObjectIdDeserializer;
import helpers.serializer.ObjectIdSerializer;
import leodagdag.play2morphia.Model;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.math.BigDecimal;

/**
 * @author f.patin
 */
@Entity(cap = @CappedAt(count = 1))
public class PayRate extends Model {
	@Id
	@JsonSerialize(using = ObjectIdSerializer.class)
	@JsonDeserialize(using = ObjectIdDeserializer.class)
	public ObjectId id;

	@Transient
	public BigDecimal extraTime125Multiplier;
	private String _extraTime125Multiplier;

	@Transient
	public BigDecimal extraTime150Multiplier;
	private String _extraTime150Multiplier;

	@Transient
	public BigDecimal sundayRateMultiplier;
	private String _sundayRateMultiplier;

	@Transient
	public BigDecimal nightlyRateMultiplier;
	private String _nightlyRateMultiplier;

	@Transient
	public BigDecimal dayOffMultiplier;
	private String _dayOffMultiplier;

	@SuppressWarnings({"unused"})
	@PrePersist
	private void prePersist() {
		if (extraTime125Multiplier != null) {
			_extraTime125Multiplier = extraTime125Multiplier.toPlainString();
		}
		if (extraTime150Multiplier != null) {
			_extraTime150Multiplier = extraTime150Multiplier.toPlainString();
		}
		if (sundayRateMultiplier != null) {
			_sundayRateMultiplier = sundayRateMultiplier.toPlainString();
		}
		if (nightlyRateMultiplier != null) {
			_nightlyRateMultiplier = nightlyRateMultiplier.toPlainString();
		}
		if (dayOffMultiplier != null) {
			_dayOffMultiplier = dayOffMultiplier.toPlainString();
		}
	}

	@SuppressWarnings({"unused"})
	@PostLoad
	private void postLoad() {
		if (_extraTime125Multiplier != null) {
			extraTime125Multiplier = new BigDecimal(_extraTime125Multiplier);
		}
		if (_extraTime150Multiplier != null) {
			extraTime150Multiplier = new BigDecimal(_extraTime150Multiplier);
		}
		if (_sundayRateMultiplier != null) {
			sundayRateMultiplier = new BigDecimal(_sundayRateMultiplier);
		}
		if (_nightlyRateMultiplier != null) {
			nightlyRateMultiplier = new BigDecimal(_nightlyRateMultiplier);
		}
		if (_dayOffMultiplier != null) {
			dayOffMultiplier = new BigDecimal(_dayOffMultiplier);
		}
	}

	private static Model.Finder<ObjectId, PayRate> find() {
		return new Model.Finder<>(ObjectId.class, PayRate.class);
	}

	public PayRate() {
	}

	public static PayRate load() {
		return find().get();
	}
}
