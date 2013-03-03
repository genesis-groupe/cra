package helpers.serializer;

import constants.Pattern;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.joda.time.Duration;

import java.io.IOException;

/**
 * User: f.patin
 * Date: 30/07/12
 * Time: 16:56
 */
public class DurationSerializer extends JsonSerializer<Duration> {

	@Override
	public void serialize(final Duration duration, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonGenerationException {
		jgen.writeString(Pattern.DURATION_FORMATTER.print(duration.toPeriod()));
	}
}
