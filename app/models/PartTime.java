package models;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.annotations.PostLoad;
import com.github.jmkgreen.morphia.annotations.PrePersist;
import com.github.jmkgreen.morphia.annotations.Transient;
import com.google.common.collect.Lists;
import constants.MissionCode;
import constants.Pattern;
import helpers.deserializer.DateTimeDeserializer;
import helpers.formatter.JodaDateTimeFormatter;
import helpers.serializer.DateTimeSerializer;
import helpers.time.TimerHelper;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

/**
 * User: LPU
 * Date: 26/10/12
 */
@Embedded
public class PartTime {

	@Transient
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime startPartTime;

	@JsonIgnore
	private Date _startPartTime;

	@Transient
	@JsonSerialize(using = DateTimeSerializer.class)
	@JsonDeserialize(using = DateTimeDeserializer.class)
	@JodaDateTimeFormatter.JodaDateTime(pattern = Pattern.DATE)
	public DateTime endPartTime;

	@JsonIgnore
	private Date _endPartTime;

	public List<Integer> daysOfWeek = Lists.newArrayList();

	public Integer repeatEveryXWeeks;

	public Boolean morning;
	public Boolean afternoon;
	public Boolean isActive;

	public PartTime() {
	}

	@SuppressWarnings({"unused"})
	@PrePersist
	private void prePersist() {
		if (startPartTime != null) {
			_startPartTime = startPartTime.toDate();
		}
		if (endPartTime != null) {
			_endPartTime = endPartTime.toDate();
		}
	}

	@SuppressWarnings({"unused"})
	@PostLoad
	private void postLoad() {
		if (_startPartTime != null) {
			startPartTime = new DateTime(_startPartTime.getTime());
		}
		if (_endPartTime != null) {
			endPartTime = new DateTime(_endPartTime.getTime());
		}
	}

	public String validate() {
		StringBuilder sb = new StringBuilder();

		if (endPartTime != null && (startPartTime.isAfter(endPartTime) || startPartTime.isEqual(endPartTime))) {
			sb.append("La date de début doit être strictement avant la date de fin");
		}
		if (startPartTime.withDayOfMonth(1).isBefore(DateTime.now().withDayOfMonth(1))) {
			sb.append("La date de début doit être supérieur ou égale au mois en cours");
		}
		return StringUtils.trimToNull(sb.toString());
	}

	public static List<Day> extract(final PartTime partTime, final Integer year, final Integer month) {
		if (partTime == null) {
			return Lists.newArrayList();
		}
		final List<DateTime> dates = Lists.newArrayList();
		final DateTime firstDayOfMonth = new DateTime(year, month, 1, 0, 0);
		final DateTime lastDayOfMonth = firstDayOfMonth.dayOfMonth().withMaximumValue();
		final List<Day> days = Lists.newArrayList();
		if (partTime.startPartTime.isAfter(lastDayOfMonth)
				|| (partTime.endPartTime != null && partTime.endPartTime.isBefore(firstDayOfMonth))) {
			return Lists.newArrayList();
		} else {
			dates.addAll(TimerHelper.getDaysOfMonthByDayOfWeek(year, month, partTime.daysOfWeek, Boolean.FALSE));
			if (partTime.repeatEveryXWeeks == 1) {
				for (DateTime date : dates) {
					if (!TimerHelper.isDayOff(date)) {
						Day day = new Day(date);
						if (Boolean.TRUE.equals(partTime.morning)) {
							day.morning = new HalfDay(Mission.findByCode(MissionCode.TP.code));
						}
						if (Boolean.TRUE.equals(partTime.afternoon)) {
							day.afternoon = new HalfDay(Mission.findByCode(MissionCode.TP.code));
						}
						days.add(day);
					}
				}
			} else {
				DateTime currentDate =
						TimerHelper.getDaysOfMonthByDayOfWeek(partTime.startPartTime.getYear(), partTime.startPartTime.getMonthOfYear(), partTime.daysOfWeek, Boolean.TRUE).get(0);
				while (currentDate.withDayOfMonth(1).isBefore(firstDayOfMonth)) {
					currentDate = currentDate.plusWeeks(partTime.repeatEveryXWeeks);
				}
				for (int i = 0, length = dates.size() - 1; i <= length; i++) {
					if (dates.get(i).isAfter(currentDate) || dates.get(i).isEqual(currentDate)) {
						if (!TimerHelper.isDayOff(currentDate)) {
							Day day = new Day(dates.get(i));
							if (Boolean.TRUE.equals(partTime.morning)) {
								day.morning = new HalfDay(Mission.findByCode(MissionCode.TP.code));
							}
							if (Boolean.TRUE.equals(partTime.afternoon)) {
								day.afternoon = new HalfDay(Mission.findByCode(MissionCode.TP.code));
							}
							days.add(day);
						}
						currentDate = currentDate.plusWeeks(partTime.repeatEveryXWeeks);
					}
				}
			}
		}
		return days;
	}
}