package models;

import cache.PayRateCache;
import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.Transient;
import com.google.common.base.Objects;

import java.math.BigDecimal;

/**
 * @author f.patin
 */
@Embedded
public class TimeCounter {

	public TimeDuration totalTimes = new TimeDuration();
	@Transient
	public BigDecimal totalTimesAmount = BigDecimal.ZERO;

	public TimeDuration normalTimes = new TimeDuration();
	@Transient
	public BigDecimal normalTimesAmount = BigDecimal.ZERO;

	public TimeDuration extraTimes125 = new TimeDuration();
	@Transient
	public BigDecimal extraTimes125Amount = BigDecimal.ZERO;

	public TimeDuration extraTimes150 = new TimeDuration();
	@Transient
	public BigDecimal extraTimes150Amount = BigDecimal.ZERO;

	public TimeDuration nightlyTimes = new TimeDuration();
	@Transient
	public BigDecimal nightlyTimesAmount = BigDecimal.ZERO;

	public TimeDuration sundayTimes = new TimeDuration();
	@Transient
	public BigDecimal sundayTimesAmount = BigDecimal.ZERO;

	public TimeDuration dayOffTimes = new TimeDuration();
	@Transient
	public BigDecimal dayOffTimesAmount = BigDecimal.ZERO;

	@Override
	public String toString() {
		return Objects.toStringHelper(this)/**/
				.add("totalTimes", this.totalTimes)/**/
				.add("totalTimesAmount", this.totalTimesAmount)/**/
				.add("normalTimes", this.normalTimes)/**/
				.add("normalTimesAmount", this.normalTimesAmount)/**/
				.add("extraTimes125", this.extraTimes125)/**/
				.add("extraTimes125Amount", this.extraTimes125Amount)/**/
				.add("extraTimes150", this.extraTimes150)/**/
				.add("extraTimes150Amount", this.extraTimes150Amount)/**/
				.add("nightlyTimes", this.nightlyTimes)/**/
				.add("nightlyTimesAmount", this.nightlyTimesAmount)/**/
				.add("sundayTimes", this.sundayTimes)/**/
				.add("sundayTimesAmount", this.sundayTimesAmount)/**/
				.toString();
	}

	public void computeAmounts(final BigDecimal hourlyRate) {
		final PayRate payRate = PayRateCache.getInstance().get();
		this.normalTimesAmount = hourlyRate.multiply(this.normalTimes.asHour);
		this.extraTimes125Amount = hourlyRate.multiply(payRate.extraTime125Multiplier).multiply(this.extraTimes125.asHour);
		this.extraTimes150Amount = hourlyRate.multiply(payRate.extraTime150Multiplier).multiply(this.extraTimes150.asHour);
		this.sundayTimesAmount = hourlyRate.multiply(payRate.sundayRateMultiplier).multiply(this.sundayTimes.asHour);
		this.nightlyTimesAmount = hourlyRate.multiply(payRate.nightlyRateMultiplier).multiply(this.nightlyTimes.asHour);
		this.dayOffTimesAmount = hourlyRate.multiply(payRate.dayOffMultiplier).multiply(this.dayOffTimes.asHour);
		this.totalTimesAmount = normalTimesAmount
				.add(extraTimes125Amount)
				.add(extraTimes150Amount)
				.add(sundayTimesAmount)
				.add(nightlyTimesAmount)
				.add(dayOffTimesAmount);
	}

	public void addCounters(final TimeCounter timeCounter) {
		if (timeCounter.extraTimes125Amount != null) {
			this.extraTimes125Amount = this.extraTimes125Amount.add(timeCounter.extraTimes125Amount);
		}
		if (timeCounter.extraTimes150Amount != null) {
			this.extraTimes150Amount = this.extraTimes150Amount.add(timeCounter.extraTimes150Amount);
		}
		if (timeCounter.sundayTimesAmount != null) {
			this.sundayTimesAmount = this.sundayTimesAmount.add(timeCounter.sundayTimesAmount);
		}
		if (timeCounter.nightlyTimesAmount != null) {
			this.nightlyTimesAmount = this.nightlyTimesAmount.add(timeCounter.nightlyTimesAmount);
		}
		if (timeCounter.dayOffTimesAmount != null) {
			this.dayOffTimesAmount = this.dayOffTimesAmount.add(timeCounter.dayOffTimesAmount);
		}
	}
}
