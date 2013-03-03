package models;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.PostLoad;
import com.github.jmkgreen.morphia.annotations.PrePersist;
import com.github.jmkgreen.morphia.annotations.Transient;
import leodagdag.play2morphia.Model;

import java.math.BigDecimal;

/**
 * @author f.patin
 */
@Embedded
public class DayCounter extends Model {

	public Integer nbTheoreticalWorkingDays;

	@Transient
	public BigDecimal nbCustomerDays = BigDecimal.ZERO;
	private String _nbCustomerDaysAsMillis = "0";

	@Transient
	public BigDecimal nbPresalesDays = BigDecimal.ZERO;
	private String _nbPresalesDaysAsMillis = "0";

	@Transient
	public BigDecimal nbInternalWorkDays = BigDecimal.ZERO;
	private String _nbInternalWorkDaysMillis = "0";

	@Transient
	public BigDecimal nbHolidays = BigDecimal.ZERO;
	private String _nbHolidaysAsMillis = "0";

	@Transient
	public BigDecimal nbUnpaidDays = BigDecimal.ZERO;
	private String _nbUnpaidDaysAsMillis = "0";

	@PostLoad
	@SuppressWarnings({"unused"})
	private void postLoad() {
		if (_nbCustomerDaysAsMillis != null) {
			this.nbCustomerDays = new BigDecimal(_nbCustomerDaysAsMillis);
		}
		if (_nbPresalesDaysAsMillis != null) {
			this.nbPresalesDays = new BigDecimal(_nbPresalesDaysAsMillis);
		}
		if (_nbInternalWorkDaysMillis != null) {
			this.nbInternalWorkDays = new BigDecimal(_nbInternalWorkDaysMillis);
		}
		if (_nbHolidaysAsMillis != null) {
			this.nbHolidays = new BigDecimal(_nbHolidaysAsMillis);
		}
		if (_nbUnpaidDaysAsMillis != null) {
			this.nbUnpaidDays = new BigDecimal(_nbUnpaidDaysAsMillis);
		}
	}

	@SuppressWarnings({"unused"})
	@PrePersist
	private void prePersist() {
		if (this.nbCustomerDays != null) {
			_nbCustomerDaysAsMillis = this.nbCustomerDays.toPlainString();
		}
		if (this.nbPresalesDays != null) {
			_nbPresalesDaysAsMillis = this.nbPresalesDays.toPlainString();
		}
		if (this.nbInternalWorkDays != null) {
			_nbInternalWorkDaysMillis = this.nbInternalWorkDays.toPlainString();
		}
		if (this.nbHolidays != null) {
			_nbHolidaysAsMillis = this.nbHolidays.toPlainString();
		}
		if (this.nbUnpaidDays != null) {
			_nbUnpaidDaysAsMillis = this.nbUnpaidDays.toPlainString();
		}
	}

	public void addCustomerDays(BigDecimal nb) {
		this.nbCustomerDays = this.nbCustomerDays.add(nb);
	}

	public void addPresalesDays(BigDecimal nb) {
		this.nbPresalesDays = this.nbPresalesDays.add(nb);
	}

	public void addInternalWorkDays(BigDecimal nb) {
		this.nbInternalWorkDays = this.nbInternalWorkDays.add(nb);
	}

	public void addHolidays(BigDecimal nb) {
		this.nbHolidays = this.nbHolidays.add(nb);
	}

	public void addUnpaidDays(BigDecimal nb) {
		this.nbUnpaidDays = this.nbUnpaidDays.add(nb);
	}

	public void add(DayCounter dayCounter) {
		this.addCustomerDays(dayCounter.nbCustomerDays);
		this.addPresalesDays(dayCounter.nbPresalesDays);
		this.addInternalWorkDays(dayCounter.nbInternalWorkDays);
		this.addHolidays(dayCounter.nbHolidays);
		this.addUnpaidDays(dayCounter.nbUnpaidDays);
	}

}
