package dto.holiday;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * User: LPU
 * Date: 03/10/12
 */
public class HolidayDataTableDTO {
	@JsonProperty
	// Le nom de l'attribut doit être le même que le paramètre sAjaxDataProp du DataTable
	// Par défaut : holidays
	public List<HolidayDTO> holidays = Lists.newArrayList();

	@Override
	public String toString() {
		return Objects.toStringHelper(this) /**/
				.add("holidays", holidays) /**/
				.toString();
	}

	public HolidayDataTableDTO() {
	}
}
