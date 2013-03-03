package models;

import com.github.jmkgreen.morphia.annotations.*;
import helpers.serializer.LocalTimeSerializer;
import helpers.time.TimerHelper;
import leodagdag.play2morphia.Model;
import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.math.BigDecimal;
import java.util.Date;

/**
 * User: leo
 * Date: 09/10/12
 * Time: 20:42
 */
@Embedded
public class Period extends Model {

	@Transient
	@JsonSerialize(using = LocalTimeSerializer.class)
	public LocalTime startTime;
	@JsonIgnore
	private Date _startTime;

	@Transient
	@JsonSerialize(using = LocalTimeSerializer.class)
	public LocalTime endTime;
	@JsonIgnore
	private Date _endTime;

	@JsonProperty("durationInDay")
	public BigDecimal durationInRealDay() {
		return TimerHelper.toRealDay(startTime, endTime);
	}

	@Reference
	public Mission mission;

	@SuppressWarnings({"unused"})
	@PrePersist
	private void prePersist() {
		if (startTime != null) {
			_startTime = startTime.toDateTimeToday().toDate();
		}
		if (endTime != null) {
			_endTime = endTime.toDateTimeToday().toDate();
		}
	}

	@SuppressWarnings({"unused"})
	@PostLoad
	private void postLoad() {
		if (_startTime != null) {
			startTime = new DateTime(_startTime.getTime()).toLocalTime();
		}
		if (_endTime != null) {
			endTime = new DateTime(_endTime.getTime()).toLocalTime();
		}
	}

	@SuppressWarnings({"unused"})
	private static Model.Finder<ObjectId, Period> find() {
		return new Model.Finder<>(ObjectId.class, Period.class);
	}

}
