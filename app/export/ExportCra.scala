package export

import com.itextpdf.text.pdf.PdfWriter
import com.itextpdf.text.{Paragraph, PageSize, Document}
import export.ExportCalendar.ExportMonth
import java.io.ByteArrayOutputStream
import vendor.{PDFData, PDFCra, PDFCraCustomer}

/**
 * @author f.patin
 */
/*
@ExportHeader(cra, customer)
@ExportCalendar(cra, customer, false)
@ExportTotal(cra, customer)
@ExportComment(cra, customer)
@ExportSignature(customer)
@ExportStandBy(cra, customer)
@ExportDetailedTimes(cra, customer)
@ExportFees(cra, customer)
*/
object ExportCra {


	def apply(data: PDFData) = {
		val os = new ByteArrayOutputStream()
		val document = common(os, data)
		document.close()
		os.toByteArray
	}

	private def common(os: ByteArrayOutputStream, data: PDFData) = {
		val document: Document = new Document(PageSize.A4.rotate())
		PdfWriter.getInstance(document, os)
		document.open()
		document.add(header(data))
		document.add(ExportMonth(data, extended = true))
		document.newPage()
		document.add(new Paragraph("Nouvelle Page"))

		document
	}
}
