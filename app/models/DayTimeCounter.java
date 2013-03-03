package models;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.PostLoad;
import com.github.jmkgreen.morphia.annotations.PrePersist;
import com.github.jmkgreen.morphia.annotations.Transient;
import constants.Pattern;
import helpers.deserializer.DateTimeDeserializer;
import helpers.formatter.JodaDateTimeFormatter;
import helpers.serializer.DateTimeSerializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;

import java.util.Date;

/**
 * @author f.patin
 */
@Embedded
public class DayTimeCounter extends TimeCounter {

	@Transient
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime date;
	private Date _date;

	@SuppressWarnings({"unused"})
	@PrePersist
	private void prePersist() {
		if (date != null) {
			_date = date.toDate();
		}
	}

	@SuppressWarnings({"unused"})
	@PostLoad
	private void postLoad() {
		if (_date != null) {
			date = new DateTime(_date.getTime());
		}
	}
}
