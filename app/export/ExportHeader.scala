package export

import models.{Cra, Customer}
import play.api.templates.Html
import helpers.formatter.Formatter

/**
 * @author f.patin
 */
object ExportHeader {

	def apply(cra: Cra, customer: Option[Customer]) = {
		Html(
			"""
			|<div class="header">
			|   <table>
			|	    <tbody>
			|		    <tr>
			|			    <td rowspan="6"><img src="/assets/images/genesis_logo_export.png"></td>
			|   			<td class="information">
			|	    			<table>
			|		    			<tbody>
			|			    			<tr>
			|				    			<th colspan="2"><h3>Rapport d'activité mensuel&nbsp;<span class="invalidCra">%s</span></h3></th>
			|					    	</tr>
			|						    <tr>
			|   							<th>Période :</th>
			|	    						<td>%s %s</td>
			|		    				</tr>
			|			    			<tr>
			|				    			<th>Collaborateur :</th>
			|					    		<td>%s</td>
			|						    </tr>
			|   						<tr>
			|	    						%s
			|		    				</tr>
			|			    		</tbody>
			|				    </table>
			|   			</td>
			|	    	</tr>
			|   	</tbody>
			|   </table>
			|</div>
			""".stripMargin.format(
				if (cra.isValidated) "" else "Attention! le CRA n'est pas validé",
				cra.month.label,
				cra.year,
				Formatter.formatUserName(cra.employee),
				customerRow(customer)))
	}

	def customerRow(customer: Option[models.Customer]) = {
		customer match {
			case Some(c) => Html("""<th>Client :</th><td>%s</td>""".format(c.name.capitalize))
			case _ => Html("""<th colspan="2">&nbsp;</th>""")
		}
	}
}
