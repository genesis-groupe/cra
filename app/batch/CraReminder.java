package batch;

import constants.BatchesKeys;
import play.Logger;

/**
 * @author f.patin
 */
@ScheduledBatch(BatchesKeys.craReminder)
public class CraReminder implements Batch {

	@Override
	public void schedule() {
		Logger.debug(String.format("Launch batch [%s]", CraReminder.class.getName()));
	}
}
