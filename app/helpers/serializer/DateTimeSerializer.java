package helpers.serializer;

import constants.Pattern;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.joda.time.DateTime;

import java.io.IOException;

/**
 * User: f.patin
 * Date: 25/07/12
 * Time: 14:13
 */
public final class DateTimeSerializer extends JsonSerializer<DateTime> {

	@Override
	public void serialize(final DateTime dateTime, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
		jgen.writeString(dateTime.toString(Pattern.DATE));
	}
}

