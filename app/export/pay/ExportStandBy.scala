package export.pay

import java.util
import models.{Mission, StandBy}
import org.bson.types.ObjectId
import scala.collection.JavaConversions._

/**
 * @author leo
 *         Date: 02/01/13
 *         Time: 11:06
 */
object ExportStandBy {

	def apply(weekNumber: Int, standBys: util.List[StandBy]) = {

		val table = """
					<table class="week standBy">
						%s
						%s
					</table>
		            """

		val thead = """
					<thead>
						<tr>
							<th colspan="2">Astreinte semaine %s</th>
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
					<th class="%s">%s</th>
					<td>%s</td>
				</tr>
		         """

		def header(weekNumber: Int) = {
			thead.format(weekNumber)
		}

		def row(customerName: Option[String], standByLabel: String) = {
			tr.format(
				customerName match {
					case Some(c) => "customer"
					case _ => ""
				},
				customerName.getOrElse("&nbsp;"),
				standByLabel
			)
		}

		implicit val toOrdering: Ordering[ObjectId] = Ordering.fromLessThan((oi1, oi2) => oi1.compareTo(oi2) > 0)

		if (!standBys.isEmpty) {
			val missions = (for (standBy <- standBys) yield {
				Mission.findById(standBy.missionId)
			}).sortBy(m => m.id)
				.map {
				m => (m.id, m)
			}.toMap

			table.format(
				header(weekNumber),
				tbody.format(
					(for ((standBy, index) <- standBys.sortBy(sb => sb.missionId).zipWithIndex) yield {
						val mission = missions.get(standBy.missionId).get
						val customerName = index match {
							case 0 => Some(mission.customerName)
							case _ => None
						}
						row(customerName, mission.standByMoments.get(standBy.standByMomentIndex).label)
					}).mkString)
			)
		} else {
			""
		}
	}


}
