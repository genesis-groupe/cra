package models;

import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.mapping.Mapper;
import com.github.jmkgreen.morphia.query.Query;
import helpers.deserializer.ObjectIdDeserializer;
import helpers.serializer.ObjectIdSerializer;
import leodagdag.play2morphia.Model;
import leodagdag.play2morphia.MorphiaPlugin;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

/**
 * User: leo
 * Date: 09/10/12
 * Time: 20:39
 */
@Entity
public class Role extends Model implements be.objectify.deadbolt.models.Role {

	@Id
	@JsonSerialize(using = ObjectIdSerializer.class)
	@JsonDeserialize(using = ObjectIdDeserializer.class)
	public ObjectId id;

	public String roleName;

	public String label;

	@Override
	public String getRoleName() {
		return roleName;
	}

	private static Query<Role> queryToFindMe(ObjectId id) {
		return MorphiaPlugin.ds().createQuery(Role.class).field(Mapper.ID_KEY).equal(id);
	}

	private static Model.Finder<ObjectId, Role> find() {
		return new Model.Finder<>(ObjectId.class, Role.class);
	}

	public static Role findByRoleName(String roleName) {
		return find()
				.field("roleName").equal(roleName)
				.get();
	}

	public static List<Role> all() {
		return find().order("label").asList();
	}

	public static Role findbyId(ObjectId id) {
		return queryToFindMe(id).get();
	}
}
