package constants;

import java.util.Arrays;
import java.util.List;

import static constants.BatchesKeys.craReminder;
import static constants.BatchesKeys.partTimeRecalculation;

/**
 * @author leo
 */
public enum ConfigurationKey {
	EMAIL("email", Arrays.asList("sender", "absence")),
	DEFAULT("default", Arrays.asList("password")),
	SMTP("smtp", Arrays.asList("mock", "host", "port", "chanel")),
	BATCH("batches", Arrays.asList(partTimeRecalculation, craReminder));

	final public String mainKey;
	final public List<String> subKey;

	ConfigurationKey(final String mainKey, final List<String> subKey) {
		this.mainKey = mainKey;
		this.subKey = subKey;
	}
}
