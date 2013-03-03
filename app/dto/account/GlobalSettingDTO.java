package dto.account;

import com.google.common.base.Objects;
import models.Employee;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * User: LPU
 * Date: 16/11/12
 */
public class GlobalSettingDTO {

	@JsonProperty
	public String email;

	@JsonProperty
	public Employee manager;

	@JsonProperty
	public boolean showStandBy;

	@Override
	public String toString() {
		return Objects.toStringHelper(this) /**/
				.add("email", email) /**/
				.add("manager", manager) /**/
				.add("showStandBy", showStandBy) /**/
				.toString();
	}

	public GlobalSettingDTO() {

	}
}
