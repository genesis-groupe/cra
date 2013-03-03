package helpers.serializer;

import constants.FeeType;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * User: LPU
 */
public class FeesTypeSerializer extends JsonSerializer<FeeType> {

	@Override
	public void serialize(FeeType feesType, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
		jsonGenerator.writeString(feesType.name());
	}
}
