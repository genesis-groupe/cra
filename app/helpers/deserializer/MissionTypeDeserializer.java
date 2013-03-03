package helpers.deserializer;

import constants.MissionType;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

/**
 * User: leodagdag
 * Date: 14/08/12
 * Time: 00:10
 */
// TODO A supprimer si pas utilisé
public class MissionTypeDeserializer extends JsonDeserializer<MissionType> {
	@Override
	public MissionType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
		return MissionType.byName(jsonParser.getText());
	}
}
