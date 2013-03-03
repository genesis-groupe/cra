package models;

import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Id;
import com.github.jmkgreen.morphia.annotations.Index;
import com.github.jmkgreen.morphia.annotations.Indexes;
import com.github.jmkgreen.morphia.mapping.Mapper;
import com.github.jmkgreen.morphia.query.Query;
import helpers.deserializer.ObjectIdDeserializer;
import helpers.serializer.ObjectIdSerializer;
import leodagdag.play2morphia.Model;
import leodagdag.play2morphia.MorphiaPlugin;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import play.libs.F;

import java.util.Comparator;
import java.util.List;

/**
 * User: leo
 */
@Entity
@Indexes({
		@Index("isGenesis")
})
public class Customer extends AbstractCrud {

	public static Customer Genesis() {
		return find().field("isGenesis").equal(true).get();
	}

	@Id
	@JsonSerialize(using = ObjectIdSerializer.class)
	@JsonDeserialize(using = ObjectIdDeserializer.class)
	public ObjectId id;

	public String code;

	public String name;

	public String finalCustomer;

	public Boolean isGenesis = Boolean.FALSE;

	public static final Comparator<? super Customer> BY_NAME = new Comparator<Customer>() {
		@Override
		public int compare(Customer c1, Customer c2) {
			return c1.name.compareTo(c2.name);
		}
	};

	private static Model.Finder<ObjectId, Customer> find() {
		return new Model.Finder<>(ObjectId.class, Customer.class);
	}

	private static Query<Customer> queryToFindMe(final ObjectId id) {
		return MorphiaPlugin.ds().createQuery(Customer.class).field(Mapper.ID_KEY).equal(id);
	}

	public Customer() {
	}

	public Customer(String name) {
		this.name = name;
	}

	public static Customer getOnlyFields(ObjectId id, String... fields) {
		return queryToFindMe(id).retrievedFields(true, fields).get();
	}

	public static Customer findById(ObjectId id) {
		return queryToFindMe(id).get();
	}

	public static Customer findByName(String name) {
		return find()
				.field("name").equal(name)
				.get();
	}

	public static List<Customer> all() {
		return find()
				.order("name")
				.asList();
	}

	public static Customer updateFields(ObjectId id, List<F.Tuple<String, Object>> fields) {
		return updateFields(Customer.class, id, fields);
	}

}
