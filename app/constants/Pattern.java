package constants;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

/**
 * User: f.patin
 * Date: 24/07/12
 * Time: 18:38
 */
public class Pattern {

	public static final String DATE = "dd/MM/yyyy";

	public static final String TIME = "HH:mm";

	public static final String DATETIME = "dd/MM/yyyy HH:mm:ss";

	public static final String CUSTOMER_MISISON_NAME = "%s (%s)";

	public static final PeriodFormatter DURATION_FORMATTER = new PeriodFormatterBuilder()/**/
			.printZeroAlways() /**/
			.minimumPrintedDigits(2) /**/
			.appendHours() /**/
			.appendSeparator(":") /**/
			.appendMinutes() /**/
			.appendSeparator(":") /**/
			.appendSeconds() /**/
			.toFormatter();

	public static final DateTimeFormatter DAY_DATE_SHORT = new DateTimeFormatterBuilder()
			.appendDayOfWeekShortText()
			.appendLiteral(" ")
			.appendDayOfMonth(0)
			.toFormatter();

	public static final DateTimeFormatter MONTH = new DateTimeFormatterBuilder()
			.appendMonthOfYearText()
			.toFormatter();

	public static String MONTH_AND_YEAR(final Integer year, final Integer month) {
		return String.format("%s %s", MONTH.print(month), year);
	}

}
