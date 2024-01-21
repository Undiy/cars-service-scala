package models

import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json._

case class Car(
                id: Long,
                registrationNumber: String,
                make: Option[String] = None,
                model: Option[String] = None,
                color: Option[String] = None,
                manufacturingYear: Option[Int] = None
              )

object CarFormat {
  private implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)
  implicit val carFormat: OFormat[Car] = Json.format[Car]
}