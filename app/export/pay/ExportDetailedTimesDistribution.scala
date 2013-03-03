package export.pay

import java.util
import models._
import play.api.templates.Html
import scala.collection.JavaConversions._

/**
 * @author f.patin
 */
object ExportDetailedTimesDistribution {

	def apply(pay: Pay, cra: Cra, customer: Option[Customer]) = {

		def content(pay: Pay, cra: Cra) = {
			val sb = StringBuilder.newBuilder
			for (week: WeekCounter <- pay.weeksCounters) yield {
				val standBy: util.List[StandBy] = cra.standBys.getOrElse(week.weekNumber, List.empty[StandBy])
				sb
					.append(ExportDetailedTimes(week))
					.append(ExportStandBy(week.weekNumber, standBy))
					.append("<br>")
			}
			sb.toString()
		}

		Html( """
				<div class="detailedTimeDistribution">
					<hr>
					<h3>Détail des heures</h3>
					%s
				</div>
		      """.format(content(pay, cra)))
	}



}
