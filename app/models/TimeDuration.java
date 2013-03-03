package models;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.PostLoad;
import com.github.jmkgreen.morphia.annotations.PrePersist;
import com.github.jmkgreen.morphia.annotations.Transient;
import com.google.common.base.Objects;
import helpers.serializer.DurationSerializer;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.Duration;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * One duration in Pay
 * <p/>
 *
 * @author f.patin
 */
@Embedded
public class TimeDuration {

	@JsonIgnore
	private static final BigDecimal DIVISOR = new BigDecimal(60 * 60);

	@Transient
	@JsonSerialize(using = DurationSerializer.class)
	public Duration duration = Duration.ZERO;
	private Long _millis = 0L;

	@Transient
	public BigDecimal asHour = BigDecimal.ZERO;

	@Override
	public String toString() {
		return Objects.toStringHelper(this)/**/
				.add("duration", this.duration)/**/
				.add("asHour", this.asHour)/**/
				.toString();
	}

	@SuppressWarnings({"unused"})
	@PrePersist
	private void prePersist() {
		if (this.duration != null) {
			_millis = this.duration.getMillis();
		}
	}

	@SuppressWarnings({"unused"})
	@PostLoad
	private void postLoad() {
		if (_millis != null) {
			this.duration = new Duration(_millis);
			this.asHour = new BigDecimal(duration.getStandardSeconds()).divide(DIVISOR, 2, RoundingMode.HALF_UP);
		}
	}

	@SuppressWarnings({"unused"})
	public TimeDuration() {
	}

	public TimeDuration(final Duration duration) {
		this.duration = duration;
		this.asHour = new BigDecimal(duration.getStandardSeconds()).divide(DIVISOR, 2, RoundingMode.HALF_UP);
	}

}
