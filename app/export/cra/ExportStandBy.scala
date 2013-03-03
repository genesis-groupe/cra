package export.cra

import models.{Mission, StandBy, Customer, Cra}
import org.bson.types.ObjectId
import play.api.templates.Html
import scala.collection.JavaConversions._

/**
 * @author f.patin
 */
object ExportStandBy {

	def apply(cra: Cra, customer: Option[Customer]) = {
		if (containsStandBy(cra)) {
			val sb = StringBuilder.newBuilder
				.append( """
			<div class="standBy">
				<hr>
				<h3>Astreinte</h3>
				%s
			</div>
				         """.format(displayStandBys(cra.standBys, customer)))
			Html(sb.toString())
		}
	}

	def containsStandBy(cra: Cra) = {
		cra.standBys.keySet.size > 0
	}

	def displayStandBys(standBys: java.util.Map[java.lang.Integer, java.util.List[StandBy]], customer: Option[Customer]) = {
		customer match {
			case Some(c) => byWeek(standBys)
			case None => byCustomers(standBys)
		}
	}

	def byCustomers(standBys: java.util.Map[java.lang.Integer, java.util.List[StandBy]]) = {
		val sb = StringBuilder.newBuilder
		val weeks = standBys.keySet
		val missions = new java.util.HashMap[ObjectId, StringBuilder]
		// Creation des tableaux par Missions
		for (week <- standBys) {
			for (standBy <- week._2) {
				if (!missions.containsKey(standBy.missionId)) {
					val mission = models.Mission.findById(standBy.missionId)
					missions.put(standBy.missionId, new StringBuilder(
						"""
                        <table>
                                <tbody>
                                    <tr>
                                        <th class="title" colspan="2">%s</th>
                                    </tr>
						""".format(mission.customerName)))
				}
			}
		}
		// Remplissage des tableaux
		for (m <- missions) {
			val mission = Mission.findById(m._1)
			var toFinish = false
			for (week <- weeks) {
				var first = true
				for (standBy <- standBys.get(week)) {
					if (standBy.missionId.equals(m._1)) {
						toFinish = true
						if (first) {
							m._2.append(
								"""
                                <tr>
                                    <th>Semaine %s :</th>
                                    <td>
								""".format(week))
							first = false
						} else {
							m._2.append(", ")
						}
						m._2.append(mission.standByMoments.reduceLeft((sbm, r) => {
							if (sbm.index == standBy.standByMomentIndex) sbm else (r)
						}).label)
					}
				}
				if (toFinish) {
					m._2.append("</td></tr>")
					toFinish = false
				}
			}
		}
		for (mission <- missions) {
			sb.append(mission._2.append(
				"""
	                    </tbody>
	                </table>
				"""))
		}
		Html(sb.toString())
	}

	def byWeek(standBys: java.util.Map[java.lang.Integer, java.util.List[StandBy]]) = {
		val weeks = standBys.keySet
		val sb = new StringBuilder("<table><tbody>")
		for (week <- weeks) {
			sb.append("<tr><th>Semaine %s</th><td>".format(week))
			for ((standBy, index) <- standBys.get(week).zipWithIndex) {
				if (index != 0) {
					sb.append(", ")
				}
				sb.append(Mission.findById(standBy.missionId).standByMoments.reduceLeft((sbm, r) => {
					if (sbm.index == standBy.standByMomentIndex) sbm else (r)
				}).label)
			}
			sb.append("</td></tr>")
		}
		sb.append("</tbody></table>")
		Html(sb.toString())
	}
}
