package helpers.transformer

import models.{Fee, Cra}
import scala.collection.JavaConversions._
import constants.FeeType
import helpers.time.TimerHelper
import collection.immutable.TreeMap
import java.{util, math}
import collection.mutable

/**
 * @author f.patin
 */
object FeeTransformer {
	def toMap(year: Int, month: Int, fees: util.List[Fee]): TreeMap[(Int, FeeType), BigDecimal] = {

		val byWeeks: Map[Int, Map[FeeType, BigDecimal]] = fees
			.groupBy(fee => fee.week.toInt)
			.map {
			fee =>
				(fee._1, fee._2
					.groupBy((f => f.`type`))
					.map(
					feeTypeTotal =>
						(feeTypeTotal._1, feeTypeTotal._2.foldLeft(BigDecimal("0"))((acc, curr) =>
							if (curr.total != null) {
								acc + curr.total
							} else {
								acc
							}
						)))
					)
		}

		val byWeeksFeeType: Map[(Int, FeeType), BigDecimal] = for (week <- byWeeks; fee <- week._2) yield ((week._1, fee._1), fee._2)
		val weeks = TimerHelper.getWeeksNumber(year, month, true)
		val feeTypes = FeeType.values()

		implicit val toOrdering: Ordering[(Int, FeeType)] = Ordering.Tuple2[Int, FeeType](Ordering.Int, Ordering.fromLessThan(_.order < _.order))
		TreeMap((for (week <- weeks; feeType <- feeTypes) yield (
			(week.toInt, feeType) -> byWeeksFeeType.get(week, feeType).getOrElse(BigDecimal("0"))
			)): _*)
	}

	def extractJourneys(cra: Cra): TreeMap[Int, List[(String, math.BigDecimal)]] = {
		val journeys: Map[Int, List[(String, math.BigDecimal)]] = cra.fees
			.groupBy(fee => fee.date.getWeekOfWeekyear)
			.map(fee =>
			(fee._1, (for (f <- fee._2) yield ((f.journey, f.kilometers))).toList)
		)
		val weeks = TimerHelper.getWeeksNumber(cra.year, cra.month.value, true)

		TreeMap((for (week <- weeks) yield {
			week.toInt -> journeys.get(week).getOrElse(List.empty)
		}): _*)
	}

}
