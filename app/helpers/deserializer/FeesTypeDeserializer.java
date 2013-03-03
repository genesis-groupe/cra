package helpers.deserializer;

import constants.FeeType;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

/**
 * User: LPU
 * Date: 04/12/12
 */
public class FeesTypeDeserializer extends JsonDeserializer<FeeType> {
	@Override
	public FeeType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
		return FeeType.byName(jsonParser.getText());
	}
}
