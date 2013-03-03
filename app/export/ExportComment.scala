package export

import models.{Customer, Cra}
import org.apache.commons.lang3.StringUtils
import play.api.templates.Html

/**
 * @author leo
 * Date: 29/12/12
 * Time: 17:42
 */
object ExportComment {
	def apply(cra: Cra, customer: Option[Customer]) = {
		customer match {
			case Some(c) => Html("")
			case _ => {
				if(StringUtils.isNotBlank(cra.comment)){
					Html(
						"""
				<div class="comment">
					<h4 class="title">Commentaire</h4>
					<pre class="content">%s</pre>
				</div>
						""".format(cra.comment)
					)
				}
			}
		}
	}
}
