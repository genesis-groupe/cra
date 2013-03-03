package export

import collection.immutable._
import com.itextpdf.text.pdf.{PdfPTable, PdfPCell}
import com.itextpdf.text.{BaseColor, Rectangle, Element, Phrase}
import constants.{Pattern, MissionType}
import helpers.time.TimerHelper
import models.{Day, HalfDay, Customer}
import org.joda.time.{DateTime, DateTimeConstants}
import scala.Some
import scala.collection.JavaConversions._
import vendor.PDFData

/**
 * @author f.patin
 */
object ExportCalendar {

	object ExportHalfDay {

		private def content(halfDay: models.HalfDay, customer: Option[models.Customer]) = {
			if (halfDay != null) {
				customer match {
					case None => {
						if (HalfDay.isSpecial(halfDay)) {
							"SPECIAL"
						} else {
							if (MissionType.CUSTOMER.equals(halfDay.mission.missionType)) {
								halfDay.mission.customerMissionName
							} else {
								halfDay.mission.code
							}
						}
					}
					case Some(c) =>
						HalfDay.totalInRealDay(halfDay).toString match {
							case "0.5" => "0.5"
							case t => t
						}
				}
			} else {
				" "
			}
		}

		def apply(halfDay: models.HalfDay, customer: Option[models.Customer]) = {
			val cell = new PdfPCell(new Phrase(content(halfDay, customer), font))
			cell.setHorizontalAlignment(Element.ALIGN_CENTER)
			cell.setBorderColor(BaseColor.GRAY)
			cell
		}
	}

	object ExportDay {

		val template = {
			val cell = new PdfPCell()
			cell.setHorizontalAlignment(Element.ALIGN_CENTER)
			cell.setBorder(0)
			cell
		}

		def header(date: DateTime) = {
			val cell = new PdfPCell(new Phrase(Pattern.DAY_DATE_SHORT.print(date), fontBold))
			cell.setBorder(0)
			cell
		}

		def morning(halfDay: models.HalfDay, customer: Option[models.Customer])={
			val cell = ExportHalfDay(halfDay, customer)
			cell.setBorderWidthBottom(0)
			cell
		}

		def afternoon(halfDay: models.HalfDay, customer: Option[models.Customer])={
			val cell = ExportHalfDay(halfDay, customer)
			cell.setBorderWidthTop(0)
			cell
		}

		lazy val empty = {
			val table = new PdfPTable(1)
			table.setWidthPercentage(100f)
			for (i <- 0 until 3) {
				table.addCell(new PdfPCell(new Phrase("")))
			}
			table
		}

		def apply(craFirstDate: DateTime, craLastDate: DateTime, day: models.Day, customer: Option[models.Customer]) = {
			val table = new PdfPTable(1)
			table.setWidthPercentage(100f)

			table.addCell(header(day.date))
			table.addCell(morning(day.morning, customer))
			table.addCell(afternoon(day.morning, customer))
			table
		}
	}

	object ExportWeek {

		def apply(craFirstDate: DateTime, craLastDate: DateTime, days: TreeSet[Day], customer: Option[Customer], extended: Boolean): Seq[PdfPTable] = {
			extended match {
				case true =>
					days.toList.map(day => ExportDay(craFirstDate, craLastDate, day, customer))
				case false => {
					0.until((days.head.date.getDayOfWeek - DateTimeConstants.MONDAY)).map(i => ExportDay.empty) ++
						days.toList.map(day => ExportDay(craFirstDate, craLastDate, day, customer))
				}
			}

		}
	}

	object ExportMonth {

		def apply(data: PDFData, extended: Boolean) = {
			val cra = data.cra
			val customer = data.cu
			val table = new PdfPTable(7)
			table.setWidthPercentage(100f)
			table.getDefaultCell.setBorder(Rectangle.NO_BORDER)
			table.setSpacingBefore(15f)

			val craFirstDate = TimerHelper.getFirstDayOfMonth(cra.year, cra.month.value)
			val craLastDate = TimerHelper.getLastDayOfMonth(cra.year, cra.month.value)
			implicit val toOrderingDay: Ordering[Day] = Ordering.fromLessThan(_.date isBefore _.date)
			val weeks = TreeMap(cra.days.groupBy(day => day.week.toInt).toList: _*)
			for (dayCell <- weeks.map(week => ExportWeek(craFirstDate, craLastDate, TreeSet(week._2.sortBy(day => day): _*), customer, extended)).flatten) {
				table.addCell(new PdfPCell(dayCell, ExportDay.template))
			}
			table
		}
	}


	def apply(data: PDFData, extended: Boolean) = {
		ExportMonth(data, extended)
	}
}
