package vendor

import export.{ExportCra, ExportPay}
import models.{Customer, Pay, Cra}

/**
 * @author f.patin
 */

trait PDFData {

	def cra: Cra

	def cu: Option[Customer]

	def p: Option[Pay]
}

case class PDFCra(cra: Cra) extends PDFData {
	def cu = None

	def p = None

}

case class PDFCraCustomer(cra: Cra, customer: Customer) extends PDFData {
	def cu = Some(customer)

	def p = None
}

case class PDFPay(cra: Cra, pay: Pay) extends PDFData {
	def cu = None

	def p = Some(pay)
}

case class PDFPayCustomer(cra: Cra, pay: Pay, customer: Customer) extends PDFData {
	def cu = Some(customer)

	def p = Some(pay)
}

object PDFv2 {
	def apply(pdfData: PDFData) = {
		ExportCra(pdfData)
	}
}
