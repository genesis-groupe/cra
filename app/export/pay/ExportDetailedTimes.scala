package export.pay

import helpers.formatter.Formatter
import Formatter._
import models.{TimeCounter, WeekCounter}
import scala.Some
import scala.collection.JavaConversions._
import helpers.formatter.Formatter

/**
 * @author leo
 *         Date: 02/01/13
 *         Time: 11:06
 */
object ExportDetailedTimes {

	def apply(week: WeekCounter) = {

		val table = """
					<table class="week times">
						%s
						%s
						%s
					</table>
		            """

		val thead = """
					<thead>
						<tr class="header top">
							<th class="line-title" rowspan="2">Sem. %s</th>
							<th class="col-total">Total</th>
							<th class="col-number">Nb H</th>
							<th class="col-total">Total</th>
							<th class="col-number">Maj. H</th>
							<th class="col-total">Total</th>
							<th class="col-number">Maj. H</th>
							<th class="col-total">Total</th>
							<th class="col-number">Maj. H nuit</th>
							<th class="col-total">Total</th>
							<th class="col-number">Maj. H Dim</th>
							<th class="col-total">Total</th>
							<th class="col-number">Maj. H JF</th>
							<th class="col-total">Total</th>
							<th class="col-total">Total</th>
						</tr>
						<tr class="header bottom">
							<th class="col-total">jour</th>
							<th class="col-number">Tx normal</th>
							<th class="col-total">€</th>
							<th class="col-number">Tx 25%2$s </th>
							<th class="col-total">€</th>
							<th class="col-number">Tx 50%2$s</th>
							<th class="col-total">€</th>
							<th class="col-number">50%2$s</th>
							<th class="col-total">€</th>
							<th class="col-number">Tx 100%2$s</th>
							<th class="col-total">€</th>
							<th class="col-number">Tx 100%2$s</th>
							<th class="col-total">€</th>
							<th class="col-total">€</th>
						</tr>
					</thead>
		            """

		val tbody = """
					<tbody>
						%s
					</tbody>
		            """

		val tr = """
				<tr>
					<th class="line-title">%s</th>
					<td class="col-total">%s</td>
					<td class="col-number">%s</td>
					<td class="col-total">%s</td>
					<td class="col-number">%s</td>
					<td class="col-total">%s</td>
					<td class="col-number">%s</td>
					<td class="col-total">%s</td>
					<td class="col-number">%s</td>
					<td class="col-total">%s</td>
					<td class="col-number">%s</td>
					<td class="col-total">%s</td>
					<td class="col-number">%s</td>
					<td class="col-total">%s</td>
					<th class="col-total">%s</th>
				</tr>
		         """

		val tfoot = """
					<tfoot>
						%s
					</tfoot>
		            """

		def header(weekNumber: Int) = {
			thead.format(weekNumber, "%")
		}

		def footer(lineTitle: String, timeCounter: TimeCounter) = {
			tfoot.format(row(lineTitle, timeCounter, Some("th")))
		}

		def row(lineTitle: String, timeCounter: TimeCounter, replaceMarkup: Option[String]) = {
			(replaceMarkup match {
				case Some(s) => tr.replaceAllLiterally("td", replaceMarkup.getOrElse("td"))
				case None => tr
			}).format(
				lineTitle,
				formatNumber(timeCounter.totalTimes.asHour),
				formatNumber(timeCounter.normalTimes.asHour),
				formatCurrency(timeCounter.normalTimesAmount),
				formatNumber(timeCounter.extraTimes125.asHour),
				formatCurrency(timeCounter.extraTimes125Amount),
				formatNumber(timeCounter.extraTimes150.asHour),
				formatCurrency(timeCounter.extraTimes150Amount),
				formatNumber(timeCounter.nightlyTimes.asHour),
				formatCurrency(timeCounter.nightlyTimesAmount),
				formatNumber(timeCounter.sundayTimes.asHour),
				formatCurrency(timeCounter.sundayTimesAmount),
				formatNumber(timeCounter.dayOffTimes.asHour),
				formatCurrency(timeCounter.dayOffTimesAmount),
				formatCurrency(timeCounter.totalTimesAmount)
			)
		}

		table.format(
			header(week.weekNumber),
			tbody.format(
				(for (dayTimeCounters <- week.daysTimeCounters) yield {
					row(DayNameMonth.print(dayTimeCounters.date), dayTimeCounters, None)
				}).mkString),
			footer("Total", week.timeCounter)
		)
	}
}
