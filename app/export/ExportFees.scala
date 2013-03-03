package export

import cache.FeeParameterCache
import collection.immutable.{TreeMap, TreeSet}
import constants.FeeType
import helpers.time.TimerHelper
import helpers.transformer.FeeTransformer
import java.math
import models.{Customer, FeesParameter, Cra}
import org.apache.commons.lang3.StringUtils
import play.api.templates.Html

/**
 * @author f.patin
 */
object ExportFees {
	def apply(cra: Cra, customer: Option[Customer]) = {
		customer match {
			case Some(c) => Html("")
			case _ => {
				if (!cra.fees.isEmpty) {
					Html( """
						<div class="fees">
							<hr>
							<h3>Frais</h3>
							%s
							<br>
							%s
						</div>
					      """.format(employeeVehicleCharacteristic(cra), fees(cra))
					)
				}
			}
		}
	}

	private def employeeVehicleCharacteristic(cra: Cra): String = {

		cra.employee.vehicle match {
			case null => ""
			case v => {
				val start = TimerHelper.getLastDayOfMonth(cra.year, cra.month.value)
				val end = TimerHelper.getLastDayOfMonth(cra.year, cra.month.value)
				val fp: FeesParameter = FeeParameterCache.get(start, end)
				val result = """
			                <table class="employee-vehicule">
			                    <tbody>
			                        <tr>
			                            <th>Véhicule</th><td>%s</td>
			                            <th>Marque</th><td>%s</td>
										<th rowspan="2">Tarifs kms</th><td rowspan="2" class="kilometer-rate">%.2f €/km</td>
			                        </tr>
									<tr>
										<th>Cylindre / CV fiscaux</th><td>%s</td>
										<th>Immatriculation</th><td>%s</td>
									</tr>
			                    </tbody>
			                </table>
				             """.format(v.`type`.label,
					v.brand,
					fp.getVehiculeReimbursementRate(v.`type`, v.power),
					v.power,
					v.matriculation)
				result
			}
		}
	}

	private def fees(cra: Cra): String = {
		val feesByWeek: TreeMap[(Int, FeeType), BigDecimal] = FeeTransformer.toMap(cra.year.toInt, cra.month.value.toInt, cra.fees)
		var header = new TreeSet[Int]()
		val feeTypeOrdering: Ordering[FeeType] = Ordering.fromLessThan(_.order < _.order)
		var lines = TreeMap[FeeType, StringBuilder]()(feeTypeOrdering)
		var totalWeeks = new TreeMap[Int, BigDecimal].empty
		var totalLine = TreeMap[FeeType, BigDecimal]()(feeTypeOrdering)
		for (((week, feeType), total) <- feesByWeek) {
			header += week
			if (!lines.contains(feeType)) {
				val l = lines + ((feeType, new StringBuilder("<tr><th>%s</th>".format(feeType.label))))
				lines = l
			}
			lines.get(feeType).get.append("<td>%s</td>".format(if (!BigDecimal("0").equals(total)) "%.2f €".format(total) else "&nbsp;"))
			totalWeeks = totalWeeks + ((week, totalWeeks.get(week).getOrElse(BigDecimal("0")) + total))
			totalLine = totalLine + ((feeType, totalLine.get(feeType).getOrElse(BigDecimal("0")) + total))
		}
		lines.foreach(line => line._2
			.append("<th>%s</th>".format(if (BigDecimal("0").equals(totalLine.get(line._1).get)) "&nbsp;" else "%.2f €".format(totalLine.get(line._1).get)))
			.append("</tr>"))

		val total = feesByWeek.foldLeft(BigDecimal("0"))((acc, curr) =>
			acc + curr._2
		)

		val footerSb = new StringBuilder( """<tr class="total"><th>Total</th>""")
		totalWeeks foreach ((totalWeek: (Int, BigDecimal)) => footerSb.append("<th>%.2f €</th>".format(totalWeek._2)))
		footerSb.append( """<th>%s</th>""".format(if (BigDecimal("0").equals(total)) "&nbsp;" else "%.2f €".format(total)))
			.append("</tr>")

		val headerSb = new StringBuilder("<tr><th>Semaine</th>")
		header foreach (week => headerSb.append("<th>%s</th>".format(week)))
		headerSb.append("<th>Total</th></tr>")


		val linesSb = StringBuilder.newBuilder
		lines.foreach(line => linesSb.append(line._2))

		val result = """
					<table class="fee-table">
				        <tbody>
				            %s
				        </tbody>
				    </table>
		             """.format(headerSb.append(journeys(cra)).append(linesSb).append(footerSb).toString())
		result
	}

	private def journeys(cra: Cra) = {
		val journeys: TreeMap[Int, List[(String, math.BigDecimal)]] = FeeTransformer.extractJourneys(cra)
		val journeysSb = new StringBuilder( """<tr class="journey top"><th rowspan="2">Trajets</th>""")
		val journeysTotalSb = new StringBuilder( """<tr class="journey bottom">""")
		for ((week, list) <- journeys) {
			journeysSb.append("<td>%s</td>".format(list.foldLeft(StringBuilder.newBuilder)((acc, curr) =>
				if (StringUtils.isNotBlank(curr._1)) {
					acc.append("-&nbsp;" + curr._1 + "<br>")
				} else {
					acc
				}
			)))
			val total: BigDecimal = list.foldLeft(BigDecimal("0"))((acc, curr) =>
				if (curr._2 != null) {
					acc + curr._2
				} else {
					acc
				}
			)
			journeysTotalSb.append("<td>%s</td>".format(if (BigDecimal("0").equals(total)) "&nbsp;" else "%.2f km".format(total)))
		}

		journeysSb.append( """<th rowspan="2">&nbsp;</th></tr>""").append(journeysTotalSb.append("</tr>"))
	}
}
