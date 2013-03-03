package helpers.formatter

import java.util.Locale
import models.User
import org.joda.time.format.DateTimeFormat

/**
 * @author leo
 *         Date: 25/12/12
 *         Time: 10:39
 */
object Formatter {

	val DayNameMonth = DateTimeFormat.forPattern("EEE dd/MM").withLocale(Locale.FRANCE)
	val DayName = DateTimeFormat.forPattern("EEE dd").withLocale(Locale.FRANCE)

	def formatUserName(user: User) = {
		List(user.firstName.capitalize, user.lastName.capitalize, "(" + user.trigramme.toUpperCase + ")").mkString("&nbsp;")
	}

	def formatEmployeeName(user: User) = {
		List(user.lastName.capitalize, user.firstName.capitalize).mkString(" ")
	}

	def formatCurrency(bd: BigDecimal) = {
		if (Zero.compare(bd) != 0) "%.2f €".format(bd) else "&nbsp;"
	}

	def formatNumber(bd: BigDecimal) = {
		if (Zero.compare(bd) != 0) "%.2f".format(bd) else "&nbsp;"
	}


}
