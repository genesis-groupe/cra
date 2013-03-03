package helpers.binder;

import org.bson.types.ObjectId;
import play.mvc.PathBindable;

/**
 * @author f.patin
 * @link http://stackoverflow.com/questions/10286725/how-to-bind-double-parameter-with-play-2-0-routing/10307927#10307927
 */
public class ObjectIdW implements PathBindable<ObjectIdW> {
	public ObjectId value;

	@Override
	public ObjectIdW bind(String key, String txt) {
		this.value = ObjectId.massageToObjectId(txt);
		return this;
	}

	@Override
	public String unbind(String key) {
		return value.toStringMongod();
	}

	@Override
	public String javascriptUnbind() {
		return value.toStringMongod();
	}
}
