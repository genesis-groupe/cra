package dto.partTime;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import models.PartTime;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * User: LPU
 * Date: 31/10/12
 */
public class PartTimeDataTableDTO {
	@JsonProperty
	// Le nom de l'attribut doit être le même que le paramètre sAjaxDataProp du DataTable
	// Par défaut : holidays
	public List<PartTime> partTimes = Lists.newArrayList();

	@Override
	public String toString() {
		return Objects.toStringHelper(this) /**/
				.add("partTimes", partTimes) /**/
				.toString();
	}

	public PartTimeDataTableDTO() {
	}

	public PartTimeDataTableDTO(List<PartTime> partTimes) {
		this.partTimes = partTimes;
	}
}
