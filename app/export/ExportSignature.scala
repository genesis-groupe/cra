package export

import models.Customer
import play.api.templates.Html

/**
 * @author f.patin
 */
object ExportSignature {

	def apply(customer: Option[Customer]) = {
		customer match {
			case Some(c) =>
				Html( """
                <hr>
                <table class="signature">
                    <tr>
                        <th>Signature client</th>
                        <th>Signature collaborateur</th>
                        <th>Signature Genesis</th>
                    </tr>
                    <tr>
                        <td colspan="3">&nbsp;</td>
                    </tr>
                </table>
                <br>
                <br>
				      """)
			case None => Html("")
		}
	}
}
