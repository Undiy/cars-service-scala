package models

import play.api.libs.json.{Json, JsonConfiguration, OFormat}
import play.api.libs.json.JsonNaming.SnakeCase

import java.time.LocalDateTime

case class CarStatistics(
                        numRecords: Int,
                        recordCreate: Option[CarStatisticsTimestamps] = None,
                        recordUpdate: Option[CarStatisticsTimestamps] = None,
                        mostCommonColor: Option[String] = None,
                        mostCommonMake: Option[String] = None
                        )

case class CarStatisticsTimestamps(
                                  first: LocalDateTime,
                                  latest: LocalDateTime
                                  )

object CarStatisticsFormat {
  private implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)
  implicit val carStatisticsTimestampsFormat: OFormat[CarStatisticsTimestamps] = Json.format[CarStatisticsTimestamps]
  implicit val carStatisticsFormat: OFormat[CarStatistics] = Json.format[CarStatistics]
}