package batch;

import com.google.common.collect.Sets;
import constants.ConfigurationKey;
import play.Configuration;
import play.Logger;
import play.Play;

import java.util.Set;

/**
 * @author f.patin
 */
public class BatchScheduler {
	private static final BatchScheduler INSTANCE = new BatchScheduler();

	public static BatchScheduler getInstance() {
		return INSTANCE;
	}

	private BatchScheduler() {
	}

	@SuppressWarnings("unchecked")
	public void scheduleBatches() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		Configuration config = Configuration.root().getConfig(ConfigurationKey.BATCH.mainKey);

		Set<String> batches = Sets.newHashSet();
		batches.addAll(Play.application().getTypesAnnotatedWith("batch", ScheduledBatch.class));

		for (String batchClass : batches) {
			Class<Batch> batch = (Class<Batch>) Class.forName(batchClass, true, Play.application().classloader());
			for (String batchName : ConfigurationKey.BATCH.subKey) {
				boolean isEnable = config.getBoolean(batchName);
				if (isEnable) {
					ScheduledBatch a = batch.getAnnotation(ScheduledBatch.class);
					if (batchName.equals(a.value())) {
						batch.newInstance().schedule();
					}
				} else {
					Logger.warn(String.format("Le batch [%s] est désactivé", batchName));
				}
			}
		}
	}
}

