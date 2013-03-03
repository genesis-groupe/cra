package vendor

import java.io.{FileInputStream, InputStreamReader, BufferedReader, File}
import models.{Mission, Customer, Role, Employee}
import play.libs.F
import security.RoleName
import cache.UserCache
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
import org.joda.time.chrono.ISOChronology
import java.util.Locale
import scala.collection.JavaConversions._
import constants.{MissionAllowanceType, MissionType}

/**
 * @author leo
 *         Date: 06/01/13
 *         Time: 19:11
 */
class CSV(val file: File, charset: String = "UTF-8", separator: Char = ';') extends Traversable[Array[String]] {
	val name = file.getName

	override def foreach[U](f: (Array[String]) => U) {
		val reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset))
		try {
			var next = true
			while (next) {
				val line = reader.readLine()
				if (line != null) {
					if (line.head != '#') {
						f(parse(line))
					}
				} else {
					next = false
				}
			}
		} finally {
			reader.close()
		}
	}

	def toMap[T, U](toPair: Array[String] => (T, U)): Map[T, U] = {
		val mapBuilder = Map.newBuilder[T, U]
		for (row <- this) mapBuilder += toPair(row)
		mapBuilder.result
	}

	private def parse(line: String): Array[String] = {
		line.split(separator)
	}
}

object CSV {
	def load(file: File) = {
		val csv = new CSV(file)
		val role = Role.findByRoleName(RoleName.COLLABORATOR)
		val rows = csv.toList
		val total = rows.size
		var created = 0
		csv.foreach {
			row =>
				created += insertRow(row, role)
		}

		UserCache.getInstance().invalidateAll()
		F.Tuple(int2Integer(created), int2Integer(total))
	}

	private val dtf: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy").withLocale(Locale.FRENCH).withChronology(ISOChronology.getInstance)

	private val _31_12_2012 = DateTime.parse("31/12/2012", dtf)

	private def insertRow(row: Array[String], role: Role) = {
		0
	}
}


