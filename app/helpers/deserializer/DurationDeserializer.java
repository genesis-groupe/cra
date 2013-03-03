package helpers.deserializer;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.joda.time.Duration;

import java.io.IOException;

/**
 * @author leo
 */
// TODO A supprimer si pas utilisé
public class DurationDeserializer extends JsonDeserializer<Duration> {
	@Override
	public Duration deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
		return Duration.parse(jsonParser.getText());
	}
}
