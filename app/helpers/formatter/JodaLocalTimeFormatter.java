package helpers.formatter;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import play.data.format.Formatters;

import java.util.Locale;

/**
 * Formatter for <code>org.joda.time.LocalTime</code> values.
 */
public class JodaLocalTimeFormatter extends Formatters.SimpleFormatter<LocalTime> {

	private final String pattern;

	/**
	 * Creates a org.joda.time.LocalTime formatter.
	 *
	 * @param pattern date pattern, as specified for {@link java.text.SimpleDateFormat}.
	 */
	public JodaLocalTimeFormatter(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * Binds the field - constructs a concrete value from submitted data.
	 *
	 * @param text   the field text
	 * @param locale the current <code>Locale</code>
	 * @return a new value
	 */
	public LocalTime parse(String text, Locale locale) throws java.text.ParseException {
		if (StringUtils.isBlank(text)) {
			return null;
		}

		return DateTimeFormat.forPattern(pattern).withLocale(locale).parseLocalTime(text);
	}

	/**
	 * Unbinds this fields - converts a concrete value to a plain string.
	 *
	 * @param localTime the value to unbind
	 * @param locale    the current <code>Locale</code>
	 * @return printable version of the value
	 */
	public String print(LocalTime localTime, Locale locale) {
		if (localTime == null) {
			return "";
		}

		return localTime.toString(pattern, locale);
	}

}
