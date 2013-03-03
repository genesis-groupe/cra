import com.itextpdf.text.pdf.{PdfPCell, PdfPTable}
import com.itextpdf.text._
import constants.Pattern
import helpers.formatter.Formatter
import java.net.URL
import play.api.Play
import scala.Some
import vendor.PDFData

/**
 * @author f.patin
 */
package object export {
	private val fontSize =  10
	private[export] val font = new Font(Font.FontFamily.HELVETICA,fontSize,Font.NORMAL)
	private[export] val fontBold = new Font(Font.FontFamily.HELVETICA,fontSize,Font.BOLD)
	private[export] val fontRedBold = new Font(Font.FontFamily.HELVETICA,fontSize,Font.BOLD, BaseColor.RED)

	private lazy val logo = {
		val u: URL = Play.current.resource("/public/images/genesis_logo_export.png").get
		val img = Image.getInstance(u)
		img.setBorder(0)
		img
	}

	private[export] def header(data: PDFData) = {

		val table = new PdfPTable(3)
		table.getDefaultCell.setBorder(Rectangle.NO_BORDER)
		table.setWidthPercentage(100f)

		val img = new PdfPCell(logo)
		img.setRowspan(4)
		img.setBorder(0)
		table.addCell(img)

		table.addCell("Rapport d'activité mensuel")
		table.addCell(new Phrase(if (data.cra.isValidated) "" else "Attention le CRA n'est pas validé", fontRedBold))
		table.addCell("Période")
		table.addCell(Pattern.MONTH_AND_YEAR(data.cra.year, data.cra.month.value))
		table.addCell("Collaborateur")
		table.addCell(Formatter.formatEmployeeName(data.cra.employee))
		data.cu match {
			case Some(c) => {
				println(c)
				table.addCell("Client")
				table.addCell(c.name)
			}
			case None => {
				val cell = new PdfPCell(new Phrase(""))
				cell.setBorder(0)
				cell.setColspan(2)
				table.addCell(cell)
			}
		}
		val chapter = new Chapter(1)
		chapter.add(table)
		chapter
	}

	private[export] def emptyLine(number: Int) = {
		val paragraph = new Paragraph()
		0.until(number).foreach(_ => paragraph.add(new Paragraph(" ")))
		paragraph
	}


}
