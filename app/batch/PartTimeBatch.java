package batch;

import constants.BatchesKeys;
import play.Logger;

/**
 * @author leo
 *         Date: 16/12/12
 *         Time: 17:00
 */
@ScheduledBatch(BatchesKeys.partTimeRecalculation)
public class PartTimeBatch implements Batch {

	@Override
	public void schedule() {
		Logger.debug(String.format("Launch batch [%s]", PartTimeBatch.class.getName()));
	    /*
        DateTime now = DateTime.now();

        DateTime plannedStart = new DateTime()
                .withHourOfDay(1)
                .withMinuteOfHour(0)
                .withSecondOfMinute(0)
                .withMillisOfSecond(0);

        DateTime nextRun = (now.isAfter(plannedStart))
                ? plannedStart.plusDays(1)
                : plannedStart;

        Long nextRunInSeconds = (long) secondsBetween(now, nextRun).getSeconds();

        Akka.system().scheduler().schedule(
                Duration.create(nextRunInSeconds, TimeUnit.SECONDS),
                Duration.create(1, TimeUnit.DAYS),
                new Runnable() {
                    public void run() {
                        List<Employee> employees = Employee.fetchWithPartTime();
                        for (Employee employee : employees) {

                        }
                    }
                }
        );
        */

	}
}
