package models;

import com.google.common.base.Objects;
import helpers.time.TimerHelper;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Comparator;

/**
 * A Month with the number and its label.
 * <p/>
 * User: f.patin
 * Date: 14/08/12
 * Time: 02:13
 */
public class Month {

	public static final Comparator<Month> MONTH = new Comparator<Month>() {
		@Override
		public int compare(final Month o1, final Month o2) {
			return o1.value.compareTo(o2.value);
		}
	};

	@JsonProperty
	public Integer value;

	@JsonProperty
	public String label;

	@Override
	public String toString() {
		return Objects.toStringHelper(this)/**/
				.add("value", value)/**/
				.add("label", label)/**/
				.toString();
	}

	@SuppressWarnings({"unused"})
	public Month() {
	}

	public Month(Integer month) {
		TimerHelper.MonthType monthType = TimerHelper.MonthType.getById(month);
		this.value = monthType.id;
		this.label = monthType.label;
	}

	public Month(TimerHelper.MonthType monthType) {
		this.value = monthType.id;
		this.label = monthType.label;
	}
}
