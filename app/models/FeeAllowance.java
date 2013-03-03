package models;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.PostLoad;
import com.github.jmkgreen.morphia.annotations.PrePersist;
import com.github.jmkgreen.morphia.annotations.Transient;
import constants.MissionAllowanceType;

import java.math.BigDecimal;

/**
 * @author f.patin
 */
@Embedded
public class FeeAllowance {

	@Transient
	public MissionAllowanceType missionAllowanceType;
	private String _feeAllowanceType;

	@Transient
	public BigDecimal amount;
	private String _amount;

	@SuppressWarnings({"unused"})
	@PrePersist
	private void prePersist() {
		if (this.amount != null) {
			_amount = this.amount.toPlainString();
		}
		if (missionAllowanceType != null) {
			_feeAllowanceType = missionAllowanceType.name();
		}
	}

	@SuppressWarnings({"unused"})
	@PostLoad
	private void postLoad() {
		if (_amount != null) {
			this.amount = new BigDecimal(_amount);
		}
		if (_feeAllowanceType != null) {
			missionAllowanceType = MissionAllowanceType.valueOf(_feeAllowanceType);
		}
	}

	@SuppressWarnings({"unused"})
	public FeeAllowance() {
	}

	public FeeAllowance(MissionAllowanceType missionAllowanceType, BigDecimal amount) {
		this.missionAllowanceType = missionAllowanceType;
		this.amount = amount;
	}
}
