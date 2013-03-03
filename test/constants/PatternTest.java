package constants;

import helpers.time.TimerHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author f.patin
 */
public class PatternTest {

	@Before
	public void setUp() {

	}

	@After
	public void tearDown() {

	}

	@Test
	public void testTime() {
		assertThat(Pattern.DURATION_FORMATTER.print(TimerHelper.WEEK_DURATION.toPeriod())).isEqualTo("37:00:00");

	}
}
