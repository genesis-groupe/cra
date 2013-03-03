package models;

import com.github.jmkgreen.morphia.annotations.PostLoad;
import com.github.jmkgreen.morphia.annotations.PrePersist;
import com.github.jmkgreen.morphia.annotations.Transient;
import constants.Pattern;
import helpers.deserializer.DateTimeDeserializer;
import helpers.formatter.JodaDateTimeFormatter;
import helpers.serializer.DateTimeSerializer;
import leodagdag.play2morphia.Model;
import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;

import java.util.Comparator;
import java.util.Date;

/**
 * @author leo
 */
public class Holiday extends HalfDay {

	public static final Comparator<Holiday> HOLIDAY_COMPARATOR = new Comparator<Holiday>() {
		@Override
		public int compare(Holiday day1, Holiday day2) {
			return DateTimeComparator.getDateOnlyInstance().compare(day1.date, day2.date);
		}
	};
	public Boolean isValidated = Boolean.TRUE;

	public Boolean isRecurrent = Boolean.FALSE;

	@Transient
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime date;

	@JsonIgnore
	private Date _date;

	public String halfDayType;

	public String comment;

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

	@SuppressWarnings({"unused"})
	public Holiday() {
	}

	public Holiday(Mission mission, final DateTime date, final String halfDayType) {
		this.mission = mission;
		this.date = date;
		this.halfDayType = halfDayType;
	}

	public Holiday(Mission mission, final DateTime date, final String halfDayType, final String comment) {
		this.mission = mission;
		this.date = date;
		this.halfDayType = halfDayType;
		this.comment = comment;
	}

	private static Model.Finder<ObjectId, Holiday> find() {
		return new Model.Finder<>(ObjectId.class, Holiday.class);
	}
}
