package export

import constants.MissionType
import helpers.time.TimerHelper
import play.api.templates.Html

/**
 * @author f.patin
 */
object ExportTotal {

	def apply(cra: models.Cra, customer: Option[models.Customer]) = {
		val sb = new StringBuilder("<br>")
		customer match {
			case None => sb.append(
				"""
            <table class="total">
                <tbody>
                    <tr>
                        <th rowspan="2" id="total">Total</th>
                        <th>Jours produits</th>
                        <th>AV</th>
                        <th>TI/F/IC</th>
                        <th>CP/RTT</th>
                        <th>TP/CSS</th>
                        <th>Jours ouvrés</th>
                    </tr>
                    <tr>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                        <td>%s</td>
                    </tr>
                </tbody>
            </table>
				""".format(
					cra.totalInRealDay(MissionType.CUSTOMER),
					cra.totalInRealDay(MissionType.PRESALES),
					cra.totalInRealDay(MissionType.INTERNAL_WORK),
					cra.totalInRealDay(MissionType.HOLIDAY),
					cra.totalInRealDay(MissionType.UNPAID),
					TimerHelper.getNbTheoreticalWorkingDays(cra.year, cra.month.value)))
			case Some(c) =>
				sb.append("<h4>Total : %s jours</h4>".format(cra.totalInRealDay))
		}
		Html(sb.toString())
	}
}
