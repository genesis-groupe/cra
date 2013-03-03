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
 * Date: 03/10/12
 */
public class HolidayDTO {
	@JsonProperty
	public String missionCode;
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime start;
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime end;

	public String halfDayType;

	public String comment;
	@JsonProperty
	public Double nbDay;
	@JsonProperty
	public Boolean isValidated;

	@Override
	public String toString() {
		return Objects.toStringHelper(this) /**/
				.add("missionCode", missionCode) /**/
				.add("start", start) /**/
				.add("end", end) /**/
				.add("halfDayType", halfDayType) /**/
				.add("nbDay", nbDay) /**/
				.add("isValidated", isValidated) /**/
				.toString();
	}

	public HolidayDTO() {

	}
}
