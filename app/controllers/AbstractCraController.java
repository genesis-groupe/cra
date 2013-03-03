package controllers;

import helpers.deserializer.ObjectIdDeserializer;
import helpers.serializer.ObjectIdSerializer;
import models.Cra;
import org.bson.types.ObjectId;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.node.JsonNodeFactory;

import static play.libs.Json.toJson;

/**
 * @author leo
 */
public abstract class AbstractCraController extends AbstractController {

	protected static class AbstractCraForm {

		public String trigramme;

		public Integer year;

		public Integer month;
		/* Id du Cra */
		@JsonSerialize(using = ObjectIdSerializer.class)
		@JsonDeserialize(using = ObjectIdDeserializer.class)
		public ObjectId id;
	}

	protected static JsonNode fetchCra(final String trigramme, final Integer year, final Integer month, Boolean extended) {
		Cra resultCra = Cra.fetch(trigramme, year, month, extended);
		return resultCra == null ? JsonNodeFactory.instance.nullNode() : toJson(resultCra);
	}

}
