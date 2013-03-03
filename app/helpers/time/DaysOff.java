package helpers.time;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

/**
 * @author leo
 */
public class DaysOff {


	public static Comparable<String> comp = new Comparable<String>() {
		@Override
		public int compareTo(String o) {
			return 0;
		}
	};

	public static Boolean isDayOff(DateTime date) {
		int dayOfYear = date.getDayOfYear();
		int monthOfYear = date.getMonthOfYear();
		int dayOfMonth = date.getDayOfMonth();
		int year = date.getYear();

		// 1er Janvier
		if (dayOfYear == 1) {
			return true;
		}

		// 1er Mai
		if (monthOfYear == 5 && dayOfMonth == 1) {
			return true;
		}

		// 8 Mai
		if (monthOfYear == 5 && dayOfMonth == 8) {
			return true;
		}

		// 14 Juiller
		if (monthOfYear == 7 && dayOfMonth == 14) {
			return true;
		}

		// 15 Août
		if (monthOfYear == 8 && dayOfMonth == 15) {
			return true;
		}

		// 1er Novembre
		if (monthOfYear == 11 && dayOfMonth == 1) {
			return true;
		}

		// 11 Novembre
		if (monthOfYear == 11 && dayOfMonth == 11) {
			return true;
		}

		// 25 Décembre
		if (monthOfYear == 12 && dayOfMonth == 25) {
			return true;
		}

		// Easter
		final DateTime easter = getEaster(year);
		final int easterMonth = easter.getMonthOfYear();
		final int easterDay = easter.getDayOfMonth();
		if (easterMonth == monthOfYear && easterDay == dayOfMonth) {
			return true;
		}
		// Easter Monday
		final DateTime easterMonday = easter.plusDays(1);
		final int easterMondayMonth = easterMonday.getMonthOfYear();
		final int easterMondayDay = easterMonday.getDayOfMonth();
		if (easterMondayMonth == monthOfYear && easterMondayDay == dayOfMonth) {
			return true;
		}
		// Ascension
		final DateTime ascension = easter.plusDays(39);
		final int ascensionMonth = ascension.getMonthOfYear();
		final int ascensionDay = ascension.getDayOfMonth();
		if (ascensionMonth == monthOfYear && ascensionDay == dayOfMonth) {
			return true;
		}

		// Pentecost
		final DateTime pentecost = easter.plusDays(49);
		final int pentecostMonth = pentecost.getMonthOfYear();
		final int pentecostDay = pentecost.getDayOfMonth();
		if (pentecostMonth == monthOfYear && pentecostDay == dayOfMonth) {
			return true;
		}

		// Pentecost Monday
		final DateTime pentecostMonday = easter.plusDays(50);
		final int pentecostMondayMonth = pentecostMonday.getMonthOfYear();
		final int pentecostMondayDay = pentecostMonday.getDayOfMonth();
		if (pentecostMondayMonth == monthOfYear && pentecostMondayDay == dayOfMonth) {
			return true;
		}

		return false;
	}

	/**
	 * Return the date of Easter for a year.
	 * http://fr.wikipedia.org/wiki/Calcul_de_la_date_de_P%C3%A2ques#Algorithme_de_Oudin
	 *
	 * @param year
	 * @return Easter as DateTime
	 */
	public static DateTime getEaster(Integer year) {
		final Integer g, c, c_4, e, h, k, p, q, i, b, j1, j2, r;
		final DateTime easter;
		g = year % 19;
		c = year / 100;
		c_4 = c / 4;
		e = (8 * c + 13) / 25;
		h = (19 * g + c - c_4 - e + 15) % 30;
		k = h / 28;
		p = 29 / (h + 1);
		q = (21 - g) / 11;
		i = (k * p * q - 1) * k + h;
		b = year / 4 + year;
		j1 = (b + i + 2 + c_4) - c;
		j2 = j1 % 7;
		r = 28 + i - j2;
		if (r > 31) {
			easter = new DateTime(year, 4, r - 31, 0, 0);
		} else {
			easter = new DateTime(year, 3, r, 0, 0);
		}
		return easter;
	}

	/**
	 * Indicate if a date is a Saturday or a Sunday
	 *
	 * @param date
	 * @return
	 */
	public static boolean isSaturdayOrSunday(DateTime date) {
		return (isSaturday(date)) || (isSunday(date));
	}

	/**
	 * Indicate if a date is a Saturday
	 *
	 * @param date
	 * @return
	 */
	public static boolean isSaturday(DateTime date) {
		return date.getDayOfWeek() == DateTimeConstants.SATURDAY;
	}

	/**
	 * Indicate if a date is a Sunday
	 *
	 * @param date
	 * @return
	 */
	public static boolean isSunday(DateTime date) {
		return date.getDayOfWeek() == DateTimeConstants.SUNDAY;
	}
}
