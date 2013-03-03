package helpers.formatter;

import org.joda.time.DateTime;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.format.DateTimeFormat;
import play.data.format.Formatters;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Locale;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation formatter, triggered by the <code>@LocalTime</code> annotation.
 */
public class JodaDateTimeFormatter extends Formatters.AnnotationFormatter<JodaDateTimeFormatter.JodaDateTime, DateTime> {

	@Target({FIELD})
	@Retention(RUNTIME)
	@play.data.Form.Display(name = "format.jodaDateTime", attributes = {"pattern"})
	public static @interface JodaDateTime {

		/**
		 * Date pattern, as specified for {@link java.text.SimpleDateFormat}.
		 */
		String pattern();
	}

	/**
	 * Binds the field - constructs a concrete value from submitted data.
	 *
	 * @param annotation the annotation that trigerred this formatter
	 * @param text       the field text
	 * @param locale     the current <code>Locale</code>
	 * @return a new value
	 */
	@Override
	public DateTime parse(JodaDateTime annotation, String text, Locale locale) throws java.text.ParseException {
		if (text == null || text.trim().isEmpty()) {
			return null;
		}

		return DateTimeFormat.forPattern(annotation.pattern()).withLocale(locale).withChronology(ISOChronology.getInstance()).parseDateTime(text);
	}

	/**
	 * Unbinds this field - converts a concrete value to plain string
	 *
	 * @param annotation the annotation that trigerred this formatter
	 * @param value      the value to unbind
	 * @param locale     the current <code>Locale</code>
	 * @return printable version of the value
	 */
	@Override
	public String print(final JodaDateTime annotation, final DateTime value, final Locale locale) {
		if (value == null) {
			return "";
		}
		return value.toString(annotation.pattern(), locale);
	}

}
