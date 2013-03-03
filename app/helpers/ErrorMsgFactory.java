package helpers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.codehaus.jackson.JsonNode;
import play.data.validation.ValidationError;

import java.util.List;
import java.util.Map;

import static play.libs.Json.toJson;

/**
 * @author leo
 */
public class ErrorMsgFactory {
	public static JsonNode asJson(String... errors) {
		Map<String, List<String>> map = Maps.newHashMap();
		map.put("", Lists.newArrayList(errors));
		return toJson(map);
	}

	public static List<String> asHtml(Map<String, List<ValidationError>> errors) {
		List<String> msgErrors = Lists.newArrayList();
		for (String key : errors.keySet()) {
			for (ValidationError error : errors.get(key)) {
				msgErrors.add(error.message());
			}
		}
		return msgErrors;
	}
}
