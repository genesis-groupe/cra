package models;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.PostLoad;
import com.github.jmkgreen.morphia.annotations.PrePersist;
import com.github.jmkgreen.morphia.annotations.Transient;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import constants.Pattern;
import helpers.deserializer.DateTimeDeserializer;
import helpers.formatter.JodaDateTimeFormatter;
import helpers.serializer.DateTimeSerializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * A week used in Pay
 * <p/>
 * User: f.patin
 * Date: 01/08/12
 * Time: 14:30
 */
@Embedded
public class WeekCounter {

	public static final Comparator<WeekCounter> BY_START_DATE = new Comparator<WeekCounter>() {
		@Override
		public int compare(WeekCounter wc1, WeekCounter wc2) {
			return wc1.startDay.compareTo(wc2.startDay);
		}
	};

	public Integer year;

	public Integer weekNumber;

	@Transient
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime startDay;
	private Date _startDay;

	@Transient
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime endDay;
	private Date _endDay;

	public TimeCounter timeCounter = new TimeCounter();

	public List<DayTimeCounter> daysTimeCounters = Lists.newArrayList();

	@SuppressWarnings({"unused"})
	@PrePersist
	private void prePersist() {
		if (startDay != null) {
			_startDay = startDay.toDate();
		}
		if (endDay != null) {
			_endDay = endDay.toDate();
		}
	}

	@SuppressWarnings({"unused"})
	@PostLoad
	private void postLoad() {
		if (_startDay != null) {
			startDay = new DateTime(_startDay.getTime());
		}
		if (_endDay != null) {
			endDay = new DateTime(_endDay.getTime());
		}
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)/**/
				.add("year", this.year)/**/
				.add("weekNumber", this.weekNumber)/**/
				.add("startDay", this.startDay)/**/
				.add("endDay", this.endDay)/**/
				.add("monthTimeCounter", this.timeCounter)/**/
				.toString();
	}

	@SuppressWarnings({"unused"})
	public WeekCounter() {
	}

	public WeekCounter(final DateTime date) {
		this.year = date.getYear();
		this.weekNumber = date.getWeekOfWeekyear();
		this.startDay = date;
		this.endDay = startDay.plusDays(6);
	}

}
