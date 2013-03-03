package dto.holiday;

import com.google.common.base.Objects;
import constants.Pattern;
import helpers.deserializer.DateTimeDeserializer;
import helpers.formatter.JodaDateTimeFormatter;
import helpers.serializer.DateTimeSerializer;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;

/**
 * User: LPU
 * Date: 19/10/12
 */
public class RecurrentDayDTO {
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime start;
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime end;
	@JsonProperty
	public Double nbDay;
	@JsonProperty
	public Boolean findDayOff;
	@JsonProperty
	public Boolean isOnlyDay;

	@Override
	public String toString() {
		return Objects.toStringHelper(this) /**/
				.add("start", start) /**/
				.add("end", end) /**/
				.add("nbDay", nbDay) /**/
				.add("findDayOff", findDayOff) /**/
				.add("isOnlyDay", isOnlyDay) /**/
				.toString();
	}

	public RecurrentDayDTO() {

	}
}
