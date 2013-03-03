package models;

import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;
import helpers.deserializer.ObjectIdDeserializer;
import helpers.serializer.ObjectIdSerializer;
import leodagdag.play2morphia.Model;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * User: leo
 * Date: 09/10/12
 * Time: 20:39
 */
@Entity
public class Permission extends Model implements be.objectify.deadbolt.models.Permission {

	@Id
	@JsonSerialize(using = ObjectIdSerializer.class)
	@JsonDeserialize(using = ObjectIdDeserializer.class)
	public ObjectId id;

	public String value;

	@Override
	public String getValue() {
		return value;
	}
}
