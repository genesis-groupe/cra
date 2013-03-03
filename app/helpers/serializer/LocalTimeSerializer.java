package helpers.serializer;

import constants.Pattern;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.joda.time.LocalTime;

import java.io.IOException;

/**
 * User: f.patin
 * Date: 25/07/12
 * Time: 14:13
 */
public final class LocalTimeSerializer extends JsonSerializer<LocalTime> {

	@Override
	public void serialize(final LocalTime localTime, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
		jgen.writeString(localTime.toString(Pattern.TIME));
	}
}

