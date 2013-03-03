package models;

import be.objectify.deadbolt.models.RoleHolder;
import cache.UserCache;
import com.github.jmkgreen.morphia.annotations.*;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import helpers.SecurityHelper;
import helpers.deserializer.ObjectIdDeserializer;
import helpers.serializer.ObjectIdSerializer;
import leodagdag.play2morphia.Model;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: f.patin
 * Date: 09/10/12
 * Time: 20:39
 */
@Entity(value = "User")
@Indexes({
		@Index("username"),
		@Index("username, password"),
		@Index("trigramme")
})
public class User extends AbstractCrud implements RoleHolder {

	@Id
	@JsonSerialize(using = ObjectIdSerializer.class)
	@JsonDeserialize(using = ObjectIdDeserializer.class)
	public ObjectId id;

	public String username;

	public String password;

	public String email;

	public String trigramme;

	public String firstName;

	public String lastName;

	@Embedded
	public Role role;

	@Embedded
	public List<Permission> permissions = new ArrayList<>();

	@Override
	@JsonIgnore
	public List<? extends Role> getRoles() {
		return Lists.newArrayList(role);
	}

	@Override
	public List<? extends Permission> getPermissions() {
		return permissions;
	}

	private static Model.Finder<ObjectId, User> find() {
		return new Model.Finder<>(ObjectId.class, User.class);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)/**/
				.add("id", id)/**/
				.add("username", username)/**/
				.add("password", password)/**/
				.add("email", email)/**/
				.add("trigramme", trigramme)/**/
				.add("firstName", firstName)/**/
				.add("lastName", lastName)/**/
				.add("role", role)/**/
				.add("permissions", permissions)/**/
				.toString();
	}

	@SuppressWarnings({"unused"})
	@PrePersist
	private void prePersist() {
		this.username = StringUtils.lowerCase(this.username);
		this.trigramme = StringUtils.upperCase(this.trigramme);
		this.firstName = StringUtils.lowerCase(this.firstName);
		this.lastName = StringUtils.lowerCase(this.lastName);
		UserCache.getInstance().invalidate(this.username);
	}

	public static User findByUserName(String username) {
		return find().field("username").equal(username.toLowerCase()).get();
	}

	public static boolean authenticate(String username, String password) {
		checkNotNull(username);
		checkNotNull(password);
		User user = UserCache.getInstance().getByUsername(username);
		return user != null && (user.username.equals(username.toLowerCase()) && user.password.equals(SecurityHelper.md5(password)));
	}

    public static List<User> fetchEmployee(){
        return null;
    }
}
