package helpers;

import com.google.common.collect.Lists;
import helpers.time.TimerHelper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.fest.assertions.Assertions.assertThat;
import static org.joda.time.DateTimeConstants.*;
import static testUtils.Constants.YEAR_2011;
import static testUtils.Constants.YEAR_2012;

/**
 * @author f.patin
 */
public class TimerHelperTest {

	private TimerHelper january2012 = new TimerHelper(YEAR_2012, TimerHelper.MonthType.JANUARY);
	private TimerHelper february2012 = new TimerHelper(YEAR_2012, TimerHelper.MonthType.FEBRUARY);
	private TimerHelper march2012 = new TimerHelper(YEAR_2012, TimerHelper.MonthType.MARCH);
	private TimerHelper april2012 = new TimerHelper(YEAR_2012, TimerHelper.MonthType.APRIL);
	private TimerHelper may2012 = new TimerHelper(YEAR_2012, TimerHelper.MonthType.MAY);
	private TimerHelper june2012 = new TimerHelper(YEAR_2012, TimerHelper.MonthType.JUNE);
	private TimerHelper july2012 = new TimerHelper(YEAR_2012, TimerHelper.MonthType.JULY);
	private TimerHelper august2012 = new TimerHelper(YEAR_2012, TimerHelper.MonthType.AUGUST);
	private TimerHelper september2012 = new TimerHelper(YEAR_2012, TimerHelper.MonthType.SEPTEMBER);
	private TimerHelper october2012 = new TimerHelper(YEAR_2012, TimerHelper.MonthType.OCTOBER);
	private TimerHelper november2012 = new TimerHelper(YEAR_2012, TimerHelper.MonthType.NOVEMBER);
	private TimerHelper december2012 = new TimerHelper(YEAR_2012, TimerHelper.MonthType.DECEMBER);

	private DateTime firstJanuary2012 = new DateTime(YEAR_2012, JANUARY, 1, 0, 0);
	private DateTime firstFebruary2012 = new DateTime(YEAR_2012, FEBRUARY, 1, 0, 0);
	private DateTime firstMarch2012 = new DateTime(YEAR_2012, MARCH, 1, 0, 0);
	private DateTime firstApril2012 = new DateTime(YEAR_2012, APRIL, 1, 0, 0);
	private DateTime firstMay2012 = new DateTime(YEAR_2012, MAY, 1, 0, 0);
	private DateTime firstJune2012 = new DateTime(YEAR_2012, JUNE, 1, 0, 0);
	private DateTime firstJuly2012 = new DateTime(YEAR_2012, JULY, 1, 0, 0);
	private DateTime firstAugust2012 = new DateTime(YEAR_2012, AUGUST, 1, 0, 0);
	private DateTime firstSeptember2012 = new DateTime(YEAR_2012, SEPTEMBER, 1, 0, 0);
	private DateTime firstOctober2012 = new DateTime(YEAR_2012, OCTOBER, 1, 0, 0);
	private DateTime firstNovember2012 = new DateTime(YEAR_2012, NOVEMBER, 1, 0, 0);
	private DateTime firstDecember2012 = new DateTime(YEAR_2012, DECEMBER, 1, 0, 0);

	private DateTime lastDayOfMay2012 = new DateTime(YEAR_2012, MAY, 31, 0, 0);

	@Before
	public void setUp() {

	}

	@After
	public void tearDown() {

	}

	@Test
	public void testTime() {
		assertThat(TimerHelper.DAY_DURATION.getStandardDays()).isEqualTo(0);
		assertThat(TimerHelper.DAY_DURATION.toStandardDays().getDays()).isEqualTo(0);
		assertThat(TimerHelper.DAY_DURATION.getStandardHours()).isEqualTo(7);
		assertThat(TimerHelper.DAY_DURATION.toStandardHours().getHours()).isEqualTo(7);
		assertThat(TimerHelper.DAY_DURATION.getStandardMinutes()).isEqualTo(444);
		assertThat(TimerHelper.DAY_DURATION.toStandardMinutes().getMinutes()).isEqualTo(444);
		assertThat(TimerHelper.DAY_DURATION.getStandardSeconds()).isEqualTo(26640);
		assertThat(TimerHelper.DAY_DURATION.toStandardSeconds().getSeconds()).isEqualTo(26640);

		assertThat(TimerHelper.WEEK_DURATION.getStandardDays()).isEqualTo(1);
		assertThat(TimerHelper.WEEK_DURATION.getStandardHours()).isEqualTo(37);
		assertThat(TimerHelper.WEEK_DURATION.getStandardMinutes()).isEqualTo(2220);
		assertThat(TimerHelper.WEEK_DURATION.getStandardSeconds()).isEqualTo(133200);
	}

	@Test
	public void testGetMonthDays() {
		assertThat(june2012.getMonthDays().size()).isEqualTo(30);
	}

	@Test
	public void testGetNbTheoreticalWorkingDays() {
		assertThat(may2012.getNbTheoreticalWorkingDays()).isEqualTo(19);
		assertThat(june2012.getNbTheoreticalWorkingDays()).isEqualTo(21);
		assertThat(july2012.getNbTheoreticalWorkingDays()).isEqualTo(22);
	}

	@Test
	public void testGetNbDaysOff() {
		assertThat(may2012.getNbDaysOff()).isEqualTo(5);
		assertThat(june2012.getNbDaysOff()).isEqualTo(0);
		assertThat(july2012.getNbDaysOff()).isEqualTo(1);
	}

	@Test
	public void testIsSaturdayOrSunday() {
		assertThat(TimerHelper.isSaturdayOrSunday(new DateTime(YEAR_2012, JULY, 28, 0, 0, 0))).isTrue();
		assertThat(TimerHelper.isSaturdayOrSunday(new DateTime(YEAR_2012, JULY, 29, 0, 0, 0))).isTrue();
		assertThat(TimerHelper.isSaturdayOrSunday(new DateTime(YEAR_2012, JULY, 30, 0, 0, 0))).isFalse();
	}

	@Test
	public void testIsDayOff() {
		assertThat(january2012.isDayOff(1)).isTrue();
		assertThat(may2012.isDayOff(1)).isTrue();
		assertThat(may2012.isDayOff(8)).isTrue();
		assertThat(july2012.isDayOff(14)).isTrue();
		assertThat(august2012.isDayOff(15)).isTrue();
		assertThat(november2012.isDayOff(1)).isTrue();
		assertThat(november2012.isDayOff(11)).isTrue();
		assertThat(december2012.isDayOff(25)).isTrue();

		// Pâques
		assertThat(april2012.isDayOff(8)).isTrue();
		// Lundi Pâques
		assertThat(april2012.isDayOff(9)).isTrue();
		// Jeudi Ascension
		assertThat(may2012.isDayOff(17)).isTrue();
		// Pentecôte
		assertThat(may2012.isDayOff(27)).isTrue();
		// Lundi Pentecôte
		assertThat(may2012.isDayOff(28)).isTrue();

		// Not cra off.
		assertThat(april2012.isDayOff(14)).isFalse();
		assertThat(december2012.isDayOff(1)).isFalse();
	}

	@Test
	public void testGetEaster() {
		// Get Easter
		assertThat(TimerHelper.getEaster(2000)).isEqualTo(new DateTime(2000, 4, 23, 0, 0));
		assertThat(TimerHelper.getEaster(2001)).isEqualTo(new DateTime(2001, 4, 15, 0, 0));
		assertThat(TimerHelper.getEaster(2002)).isEqualTo(new DateTime(2002, 3, 31, 0, 0));
		assertThat(TimerHelper.getEaster(2003)).isEqualTo(new DateTime(2003, 4, 20, 0, 0));
		assertThat(TimerHelper.getEaster(2004)).isEqualTo(new DateTime(2004, 4, 11, 0, 0));
		assertThat(TimerHelper.getEaster(2005)).isEqualTo(new DateTime(2005, 3, 27, 0, 0));
		assertThat(TimerHelper.getEaster(2006)).isEqualTo(new DateTime(2006, 4, 16, 0, 0));
		assertThat(TimerHelper.getEaster(2007)).isEqualTo(new DateTime(2007, 4, 8, 0, 0));
		assertThat(TimerHelper.getEaster(2008)).isEqualTo(new DateTime(2008, 3, 23, 0, 0));
		assertThat(TimerHelper.getEaster(2009)).isEqualTo(new DateTime(2009, 4, 12, 0, 0));
		assertThat(TimerHelper.getEaster(2010)).isEqualTo(new DateTime(2010, 4, 4, 0, 0));
		assertThat(TimerHelper.getEaster(2011)).isEqualTo(new DateTime(2011, 4, 24, 0, 0));
		assertThat(TimerHelper.getEaster(2012)).isEqualTo(new DateTime(2012, 4, 8, 0, 0));
		assertThat(TimerHelper.getEaster(2013)).isEqualTo(new DateTime(2013, 3, 31, 0, 0));
		assertThat(TimerHelper.getEaster(2014)).isEqualTo(new DateTime(2014, 4, 20, 0, 0));
		assertThat(TimerHelper.getEaster(2015)).isEqualTo(new DateTime(2015, 4, 5, 0, 0));
		assertThat(TimerHelper.getEaster(2016)).isEqualTo(new DateTime(2016, 3, 27, 0, 0));
		assertThat(TimerHelper.getEaster(2017)).isEqualTo(new DateTime(2017, 4, 16, 0, 0));
		assertThat(TimerHelper.getEaster(2018)).isEqualTo(new DateTime(2018, 4, 1, 0, 0));
		assertThat(TimerHelper.getEaster(2019)).isEqualTo(new DateTime(2019, 4, 21, 0, 0));
		assertThat(TimerHelper.getEaster(2020)).isEqualTo(new DateTime(2020, 4, 12, 0, 0));
		assertThat(TimerHelper.getEaster(2021)).isEqualTo(new DateTime(2021, 4, 4, 0, 0));
		assertThat(TimerHelper.getEaster(2022)).isEqualTo(new DateTime(2022, 4, 17, 0, 0));
	}

	@Test
	public void testGetMondayOfDate() {
		// getFirstMondayForWeekComputation
		assertThat(TimerHelper.getMondayOfDate(firstJanuary2012)).isEqualTo(new DateTime(YEAR_2011, DECEMBER, 26, 0, 0));
		assertThat(TimerHelper.getMondayOfDate(firstFebruary2012)).isEqualTo(new DateTime(YEAR_2012, JANUARY, 30, 0, 0));
		assertThat(TimerHelper.getMondayOfDate(firstMarch2012)).isEqualTo(new DateTime(YEAR_2012, FEBRUARY, 27, 0, 0));
		assertThat(TimerHelper.getMondayOfDate(firstApril2012)).isEqualTo(new DateTime(YEAR_2012, MARCH, 26, 0, 0));
		assertThat(TimerHelper.getMondayOfDate(firstMay2012)).isEqualTo(new DateTime(YEAR_2012, APRIL, 30, 0, 0));
		assertThat(TimerHelper.getMondayOfDate(firstJune2012)).isEqualTo(new DateTime(YEAR_2012, MAY, 28, 0, 0));
		assertThat(TimerHelper.getMondayOfDate(firstJuly2012)).isEqualTo(new DateTime(YEAR_2012, JUNE, 25, 0, 0));
		assertThat(TimerHelper.getMondayOfDate(firstAugust2012)).isEqualTo(new DateTime(YEAR_2012, JULY, 30, 0, 0));
		assertThat(TimerHelper.getMondayOfDate(firstSeptember2012)).isEqualTo(new DateTime(YEAR_2012, AUGUST, 27, 0, 0));
		assertThat(TimerHelper.getMondayOfDate(firstOctober2012)).isEqualTo(new DateTime(YEAR_2012, OCTOBER, 1, 0, 0));
		assertThat(TimerHelper.getMondayOfDate(firstNovember2012)).isEqualTo(new DateTime(YEAR_2012, OCTOBER, 29, 0, 0));
		assertThat(TimerHelper.getMondayOfDate(firstDecember2012)).isEqualTo(new DateTime(YEAR_2012, NOVEMBER, 26, 0, 0));
	}

	@Test
	public void testGetSundayOfWeek() {
		assertThat(january2012.getSundayOfWeek(firstJanuary2012)).isEqualTo(new DateTime(YEAR_2012, JANUARY, 1, 0, 0));
		assertThat(february2012.getSundayOfWeek(firstFebruary2012)).isEqualTo(new DateTime(YEAR_2012, FEBRUARY, 5, 0, 0));
		assertThat(march2012.getSundayOfWeek(firstMarch2012)).isEqualTo(new DateTime(YEAR_2012, MARCH, 4, 0, 0));
		assertThat(april2012.getSundayOfWeek(firstApril2012)).isEqualTo(new DateTime(YEAR_2012, APRIL, 1, 0, 0));
		assertThat(may2012.getSundayOfWeek(firstMay2012)).isEqualTo(new DateTime(YEAR_2012, MAY, 6, 0, 0));
		assertThat(june2012.getSundayOfWeek(firstJune2012)).isEqualTo(new DateTime(YEAR_2012, JUNE, 3, 0, 0));
		assertThat(july2012.getSundayOfWeek(firstJuly2012)).isEqualTo(new DateTime(YEAR_2012, JULY, 1, 0, 0));
		assertThat(august2012.getSundayOfWeek(firstAugust2012)).isEqualTo(new DateTime(YEAR_2012, AUGUST, 5, 0, 0));
		assertThat(september2012.getSundayOfWeek(firstSeptember2012)).isEqualTo(new DateTime(YEAR_2012, SEPTEMBER, 2, 0, 0));
		assertThat(october2012.getSundayOfWeek(firstOctober2012)).isEqualTo(new DateTime(YEAR_2012, OCTOBER, 7, 0, 0));
		assertThat(november2012.getSundayOfWeek(firstNovember2012)).isEqualTo(new DateTime(YEAR_2012, NOVEMBER, 4, 0, 0));
		assertThat(december2012.getSundayOfWeek(firstDecember2012)).isEqualTo(new DateTime(YEAR_2012, DECEMBER, 2, 0, 0));
		assertThat(may2012.getSundayOfWeek(lastDayOfMay2012)).isEqualTo(new DateTime(YEAR_2012, JUNE, 3, 0, 0));
	}

	@Test
	public void testCompleteToCalendar() {
		assertThat(may2012.completeToCalendar(may2012.getMonthDays()).size()).isEqualTo(35);
		assertThat(august2012.completeToCalendar(august2012.getMonthDays()).size()).isEqualTo(35);
	}

	@Test
	public void testToGenesisDay() {
		assertThat(TimerHelper.toGenesisDay(new LocalTime(0, 0), new LocalTime(7, 24))).isEqualTo(new BigDecimal("1.00"));
		assertThat(TimerHelper.toGenesisDay(new LocalTime(0, 0), new LocalTime(3, 42))).isEqualTo(new BigDecimal("0.50"));
		assertThat(TimerHelper.toGenesisDay(new LocalTime(0, 0), new LocalTime(6, 0))).isEqualTo(new BigDecimal("0.81"));
		assertThat(TimerHelper.toGenesisDay(new LocalTime(0, 0), new LocalTime(5, 0))).isEqualTo(new BigDecimal("0.68"));
		assertThat(TimerHelper.toGenesisDay(new LocalTime(0, 0), new LocalTime(4, 0))).isEqualTo(new BigDecimal("0.54"));
		assertThat(TimerHelper.toGenesisDay(new LocalTime(0, 0), new LocalTime(3, 0))).isEqualTo(new BigDecimal("0.41"));
		assertThat(TimerHelper.toGenesisDay(new LocalTime(0, 0), new LocalTime(2, 0))).isEqualTo(new BigDecimal("0.27"));
		assertThat(TimerHelper.toGenesisDay(new LocalTime(0, 0), new LocalTime(1, 30))).isEqualTo(new BigDecimal("0.20"));
		assertThat(TimerHelper.toGenesisDay(new LocalTime(0, 0), new LocalTime(1, 0))).isEqualTo(new BigDecimal("0.14"));
	}

	@Test
	public void testGetWeeksNumber() {
		assertThat(TimerHelper.getWeeksNumber(2012, DateTimeConstants.NOVEMBER, false).size()).isEqualTo(4);
		assertThat(TimerHelper.getWeeksNumber(2012, DateTimeConstants.NOVEMBER, false).get(0)).isEqualTo(44);
		assertThat(TimerHelper.getWeeksNumber(2012, DateTimeConstants.NOVEMBER, false).get(TimerHelper.getWeeksNumber(2012, 11, false).size() - 1)).isEqualTo(47);
		assertThat(TimerHelper.getWeeksNumber(2012, DateTimeConstants.NOVEMBER, true).size()).isEqualTo(5);
		assertThat(TimerHelper.getWeeksNumber(2012, DateTimeConstants.NOVEMBER, true).get(0)).isEqualTo(44);
		assertThat(TimerHelper.getWeeksNumber(2012, DateTimeConstants.NOVEMBER, true).get(TimerHelper.getWeeksNumber(2012, 11, true).size() - 1)).isEqualTo(48);
	}

	@Test
	public void testGetDaysOfMonthByDayOfWeek() {
		assertThat(TimerHelper.getDaysOfMonthByDayOfWeek(2012, DateTimeConstants.NOVEMBER, Lists.newArrayList(DateTimeConstants.WEDNESDAY, DateTimeConstants.FRIDAY), Boolean.FALSE).size()).isEqualTo(9);
		assertThat(TimerHelper.getDaysOfMonthByDayOfWeek(2012, DateTimeConstants.NOVEMBER, Lists.newArrayList(DateTimeConstants.THURSDAY), Boolean.FALSE).size()).isEqualTo(5);
		assertThat(TimerHelper.getDaysOfMonthByDayOfWeek(2012, DateTimeConstants.NOVEMBER, Lists.newArrayList(DateTimeConstants.THURSDAY), Boolean.TRUE).size()).isEqualTo(4);
	}
}