package dto.common;

import helpers.deserializer.ObjectIdDeserializer;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * @author leo
 *         Date: 12/01/13
 *         Time: 15:36
 */
public class IdDTO {
	@JsonDeserialize(using = ObjectIdDeserializer.class)
	public ObjectId id;
}
