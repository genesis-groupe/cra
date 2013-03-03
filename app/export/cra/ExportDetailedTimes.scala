package export.cra

import constants._
import models.{Period, HalfDay, Cra, Customer}
import org.bson.types.ObjectId
import play.api.templates.Html
import scala.collection.JavaConversions._

/**
 * @author f.patin
 */
object ExportDetailedTimes {

	def apply(cra: Cra, customer: Option[Customer]) = {
		if (containsDetailedTimes(cra)) {
			val sb = StringBuilder.newBuilder
				.append( """
					<div class='detailedTime'>
						<hr>
						<h3>Détail des heures effectives</h3>
						%s
					</div>
				         """.format(display(cra, customer)))
			Html(sb.toString())
		}
	}

	def display(cra: Cra, customer: Option[Customer]) = {
		customer match {
			case Some(c) => byDay(cra)
			case None => byCustomers(cra)
		}
	}

	def containsDetailedTimes(cra: Cra) = {
		cra.days.filter(day => {
			day.isSpecial
		}).size > 0
	}

	def byDay(cra: models.Cra) = {
		val sb = new StringBuilder
		for (day <- cra.days) {
			if (day.isSpecial) {
				sb.append(
					"""
   					<table>
	                    <tbody>
		                    <tr>
		                        <th>%s</th>
					""".format(day.date.toString(constants.Pattern.DATE)))
					.append(displayDayPeriods(day))
					.append(
					"""
						</tr>
							</tbody>
							</table>
					""")
			}
		}
		Html(sb.toString())
	}

	def displayDayPeriods(day: models.Day) = {
		val sb = new StringBuilder()
		sb.append(displayHalfDayPeriods(day.morning))
		sb.append(displayHalfDayPeriods(day.afternoon))
		sb
	}

	def displayHalfDayPeriods(halfDay: HalfDay) = {
		val sb = new StringBuilder()
		if (HalfDay.isSpecial(halfDay)) {
			for (period <- halfDay.periods) {
				sb.append("<td>%s -> %s</td>".format(period.startTime.toString(Pattern.TIME), period.endTime.toString(Pattern.TIME)))
			}
		}
		sb
	}

	def byCustomers(cra: models.Cra) = {
		val sb = new StringBuilder
		for (day <- cra.days) {
			if (day.isSpecial) {
				sb.append(
					"""
   					<table>
   					    <tbody>
   					        <tr>
   					            <th class='title'>%s</th>
   					        </tr>
					""".format(day.date.toString(Pattern.DATE)))
					.append(dayByCustomer(day))
					.append("</tbody></table>")
			}
		}
		Html(sb.toString())
	}


	def dayByCustomer(day: models.Day) = {
		val sb = StringBuilder.newBuilder
		val customers = extractCustomer(day.morning) ++ extractCustomer(day.afternoon)
		val periods = extractPeriods(day.morning) ++ extractPeriods(day.morning)
		for (customer <- customers) {
			sb
				.append("<tr>")
				.append("<th>%s</th>".format(customer._2))
			for (period <- periods) {
				if (period.mission.customerId.equals(customer._1)) {
					sb.append("<td>%s -> %s</td>".format(period.startTime.toString(Pattern.TIME), period.endTime.toString(Pattern.TIME)))
				}
			}
			sb.append("</tr>")
		}
		Html(sb.toString())
	}

	def extractCustomer(halfDay: HalfDay) = {
		if (HalfDay.isSpecial(halfDay)) {
			halfDay.periods.map(p => (p.mission.customerId, p.mission.customerName)).toMap
		} else {
			scala.collection.immutable.Map.empty
		}
	}

	def extractPeriods(halfDay: HalfDay) = {
		var result = List[Period]()
		if (HalfDay.isSpecial(halfDay)) {
			result = result ++ halfDay.periods
		}
		result
	}

	def halfDayByCustomer(halfDay: HalfDay) = {
		val result = Map[ObjectId, StringBuilder]()
		if (HalfDay.isSpecial(halfDay)) {
			for (period <- halfDay.periods) {
				if (!result.containsKey(period.mission.customerId)) {
					result.put(period.mission.customerId, new StringBuilder("<th>%s</th>".format(period.mission.customerName)))
				}
				val sb: StringBuilder = result.get(period.mission.customerId).get
				sb.append("<td>%s -> %s</td>".format(period.startTime.toString(constants.Pattern.TIME), period.endTime.toString(constants.Pattern.TIME)))
			}
		}
		result
	}
}

