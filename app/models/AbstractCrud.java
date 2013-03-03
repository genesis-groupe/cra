package models;

import com.github.jmkgreen.morphia.mapping.Mapper;
import com.github.jmkgreen.morphia.query.Query;
import com.github.jmkgreen.morphia.query.UpdateOperations;
import leodagdag.play2morphia.Model;
import leodagdag.play2morphia.MorphiaPlugin;
import org.bson.types.ObjectId;
import play.libs.F;

import java.util.List;

/**
 * @author leo
 */
public abstract class AbstractCrud extends Model {

	private static <T> Query<T> query(Class<T> clazz, ObjectId id) {
		return MorphiaPlugin.ds().createQuery(clazz).field(Mapper.ID_KEY).equal(id);
	}

	public static <T> T updateFields(Class<T> clazz, ObjectId id, List<F.Tuple<String, Object>> fields) {

		UpdateOperations<T> op = MorphiaPlugin.ds().createUpdateOperations(clazz);
		for (F.Tuple<String, Object> tuple : fields) {
			if (tuple._2 != null) {
				op.set(tuple._1, tuple._2);
			}
		}
		return MorphiaPlugin.ds().findAndModify(query(clazz, id), op);
	}
}
