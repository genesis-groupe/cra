package models;

import business.PaysBiz;
import com.github.jmkgreen.morphia.annotations.*;
import com.github.jmkgreen.morphia.mapping.Mapper;
import com.github.jmkgreen.morphia.query.Query;
import com.github.jmkgreen.morphia.query.UpdateOperations;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import helpers.deserializer.ObjectIdDeserializer;
import helpers.exception.CraNotFoundException;
import helpers.exception.PayIllegalStateException;
import helpers.serializer.ObjectIdSerializer;
import helpers.time.TimerHelper;
import leodagdag.play2morphia.Model;
import leodagdag.play2morphia.MorphiaPlugin;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * The calculated pay
 *
 * @author f.patin
 *         Date: 30/07/12
 *         Time: 16:51
 */
@Entity
@Indexes({
		@Index("employee"),
		@Index("employee, year, _month")
})
public class Pay extends Model {

	@Id
	@JsonSerialize(using = ObjectIdSerializer.class)
	@JsonDeserialize(using = ObjectIdDeserializer.class)
	public ObjectId id;

	@Reference
	public Employee employee;

	public Integer year;

	@Transient
	public Month month;
	private Integer _month;

	public Boolean isValidated = Boolean.FALSE;

	@JsonSerialize(using = ObjectIdSerializer.class)
	@JsonDeserialize(using = ObjectIdDeserializer.class)
	public ObjectId idCra;

	@Transient
	public Boolean isCraValidated = Boolean.FALSE;

	@Transient
	public String craComment;

	@Embedded
	public Map<Integer, List<StandBy>> standBys = Maps.newHashMap();

	@Embedded
	public List<Fee> fees = Lists.newArrayList();

	@Embedded
	public TimeCounter monthTimeCounter = new TimeCounter();

	@Embedded
	public DayCounter monthDayCounter = new DayCounter();

	@Embedded
	public List<WeekCounter> weeksCounters = Lists.newArrayList();

	@Embedded
	public List<MissionCounter> missionCounters = Lists.newArrayList();

	@Transient
	public BigDecimal hourlyRate = BigDecimal.ZERO;

	@SuppressWarnings({"unused"})
	@PrePersist
	private void prePersist() {
		if (month != null) {
			_month = month.value;
		}
	}

	@SuppressWarnings({"unused"})
	@PostLoad
	private void postLoad() {
		if (_month != null) {
			month = new Month(TimerHelper.MonthType.getById(_month));
		}
	}

	private static Model.Finder<ObjectId, Pay> find() {
		return new Model.Finder<>(ObjectId.class, Pay.class);
	}

	private static Query<Pay> queryToFindMe(ObjectId id) {
		return MorphiaPlugin.ds().createQuery(Pay.class).field(Mapper.ID_KEY).equal(id);
	}

	private static Query<Pay> queryToFindMe(final String trigramme, final Integer year, final Integer month) {
		return MorphiaPlugin.ds().createQuery(Pay.class)
				.field("employee").equal(Employee.findByTrigramme(trigramme))
				.field("year").equal(year)
				.field("_month").equal(month);
	}

	@SuppressWarnings({"unused"})
	public Pay() {
	}

	public Pay(Employee employee, Integer year, Integer month) {
		this.employee = employee;
		this.year = year;
		this.month = new Month(month);
	}

	public static void safeDelete(final String trigramme, final Integer year, final Integer month) {
		Pay pay = Pay.find(trigramme, year, month);
		if (pay != null) {
			pay.delete();
		}
	}

	public static Pay validate(ObjectId id) {
		UpdateOperations<Pay> op = MorphiaPlugin.ds().createUpdateOperations(Pay.class).set("isValidated", true);
		return MorphiaPlugin.ds().findAndModify(queryToFindMe(id), op);
	}

	public static Pay invalidate(ObjectId id) {
		UpdateOperations<Pay> op = MorphiaPlugin.ds().createUpdateOperations(Pay.class).set("isValidated", false);
		return MorphiaPlugin.ds().findAndModify(queryToFindMe(id), op);
	}

	public static Pay invalidate(final String trigramme, final Integer year, final Integer month) {
		UpdateOperations<Pay> op = MorphiaPlugin.ds().createUpdateOperations(Pay.class).set("isValidated", false);
		return MorphiaPlugin.ds().findAndModify(queryToFindMe(trigramme, year, month), op);
	}

	public static Pay find(final String trigramme, final Integer year, final Integer month) {
		return find()
				.field("employee").equal(Employee.findByTrigramme(trigramme))
				.field("year").equal(year)
				.field("_month").equal(month)
				.get();
	}

	public void computeAmountCounter(final BigDecimal hourlyRate) {
		this.hourlyRate = hourlyRate;
		for (WeekCounter weekCounter : this.weeksCounters) {
			for(TimeCounter dayTimeCounter : weekCounter.daysTimeCounters){
				dayTimeCounter.computeAmounts(hourlyRate);
			}
			weekCounter.timeCounter.computeAmounts(this.hourlyRate);
			this.monthTimeCounter.addCounters(weekCounter.timeCounter);
		}
	}

	public static Boolean isValidated(final String trigramme, final Integer year, final Integer month) {
		final Pay pay = find()
				.field("employee").equal(Employee.findByTrigramme(trigramme))
				.field("year").equal(year)
				.field("_month").equal(month)
				.get();
		return pay != null ? pay.isValidated : Boolean.FALSE;
	}

	public static Pay getOrCompute(String trigramme, Integer year, Integer month, BigDecimal hourlyRate) throws CraNotFoundException {
		return PaysBiz.getOrCompute(trigramme, year, month, hourlyRate);
	}

	public static Pay getForExport(String trigramme, Integer year, Integer month, BigDecimal hourlyRate) throws PayIllegalStateException {
		return PaysBiz.getForExport(trigramme, year, month, hourlyRate);
	}
}
