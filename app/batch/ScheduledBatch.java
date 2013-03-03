package batch;

import java.lang.annotation.*;

/**
 * @author f.patin
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ScheduledBatch {
	String value();
}
