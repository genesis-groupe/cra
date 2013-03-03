package helpers.serializer;

import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * User: leo
 * Date: 12/10/12
 * Time: 01:06
 */
public class ObjectIdSerializer extends JsonSerializer<ObjectId> {
	@Override
	public void serialize(ObjectId objectId, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
		jsonGenerator.writeString(objectId.toStringMongod());
	}
}
