package export

import models.{Pay, Customer, Cra}
import com.itextpdf.text.{Paragraph, Document}
import java.io.ByteArrayOutputStream
import com.itextpdf.text.pdf.PdfWriter
import vendor.{PDFPayCustomer, PDFPay}

/**
 * @author f.patin
 */
object ExportPay {
	def apply(data: PDFPay) = {
		val document: Document = new Document()
		val os = new ByteArrayOutputStream()
		PdfWriter.getInstance(document, os)
		document.open()
		document.add(new Paragraph("Hello PDFCra"))
		document.close()
		os.toByteArray
	}

	def apply(data: PDFPayCustomer) = {
		val document: Document = new Document()
		val os = new ByteArrayOutputStream()
		PdfWriter.getInstance(document, os)
		document.open()
		document.add(new Paragraph("Hello PDFCra"))
		document.close()
		os.toByteArray
	}
}
