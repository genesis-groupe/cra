package models;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.junit.Test;
import testUtils.AbstractTest;

import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author f.patin
 */
public class PartTimeTest extends AbstractTest {


	@Override
	protected String getConnexionRole() {
		return null;
	}

	@Override
	protected void postBeforeEachTest() {
	}

	@Override
	protected void preAfterEachTest() {
	}

	/**
	 * Test d'un TP tous les mercredis, cas simple
	 */
	@Test
	public void testExtractEveryWeeks() {

        /* Preparation */
		PartTime pt = new PartTime();
		pt.startPartTime = new DateTime(2012, DateTimeConstants.OCTOBER, 1, 0, 0);
		pt.endPartTime = null;
		pt.isActive = Boolean.TRUE;
		pt.afternoon = Boolean.TRUE;
		pt.morning = Boolean.TRUE;
		pt.repeatEveryXWeeks = 1;
		pt.daysOfWeek = Lists.newArrayList(DateTimeConstants.WEDNESDAY); // All wednesday

        /* Test */
		List<Day> days = PartTime.extract(pt, 2012, DateTimeConstants.AUGUST);
		assertThat(days.size()).isEqualTo(0);
		days = PartTime.extract(pt, 2012, DateTimeConstants.NOVEMBER);
		assertThat(days.size()).isEqualTo(4);
	}

	/**
	 * Test d'un TP toutes les deux semaines le mercredi
	 */
	@Test
	public void testExtractEveryTwoWeeks() {

        /* Preparation */
		PartTime pt = new PartTime();
		pt.startPartTime = new DateTime(2012, DateTimeConstants.OCTOBER, 1, 0, 0);
		pt.endPartTime = null;
		pt.isActive = Boolean.TRUE;
		pt.afternoon = Boolean.TRUE;
		pt.morning = Boolean.TRUE;
		pt.repeatEveryXWeeks = 2;
		pt.daysOfWeek = Lists.newArrayList(DateTimeConstants.WEDNESDAY); // All wednesday

        /* Test */
		List<Day> days = PartTime.extract(pt, 2012, DateTimeConstants.AUGUST);
		assertThat(days.size()).isEqualTo(0);
		days = PartTime.extract(pt, 2012, DateTimeConstants.NOVEMBER);
		assertThat(days.size()).isEqualTo(2);
	}

}