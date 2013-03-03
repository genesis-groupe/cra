package models;

import com.github.jmkgreen.morphia.annotations.*;
import constants.Pattern;
import helpers.deserializer.DateTimeDeserializer;
import helpers.formatter.JodaDateTimeFormatter;
import helpers.serializer.DateTimeSerializer;
import leodagdag.play2morphia.Model;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;

import java.util.Comparator;
import java.util.Date;

/**
 * @author f.patin
 */
@Embedded
public class AffectedMission extends Model {
	public static final Comparator<AffectedMission> BY_CUSTOMER_NAME = new Comparator<AffectedMission>() {
		@Override
		public int compare(AffectedMission m1, AffectedMission m2) {
			return m1.mission.customerMissionName.compareTo(m2.mission.customerMissionName);
		}
	};

	@Reference
	public Mission mission;

	@Transient
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime startDate;
	private Date _startDate;

	@Transient
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime endDate;
	private Date _endDate;

	@SuppressWarnings({"unused"})
	public AffectedMission() {
	}

	public AffectedMission(final Mission mission) {
		this.mission = mission;
		this.startDate = mission.startDate;
		this.endDate = mission.endDate;
	}

	public AffectedMission(final Mission mission, final DateTime startDate, final DateTime endDate) {
		this.mission = mission;
		this.startDate = startDate;
		this.endDate = endDate;
	}

	@SuppressWarnings({"unused"})
	@PrePersist
	private void prePersist() {
		if (startDate != null) {
			_startDate = startDate.toDate();
		}
		if (endDate != null) {
			_endDate = endDate.toDate();
		}
	}

	@SuppressWarnings({"unused"})
	@PostLoad
	private void postLoad() {
		if (_startDate != null) {
			startDate = new DateTime(_startDate.getTime());
		}
		if (_endDate != null) {
			endDate = new DateTime(_endDate.getTime());
		}
	}
}
