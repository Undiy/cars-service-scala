package models

import play.api.libs.functional.syntax._
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

object Car {
  implicit val config: JsonConfiguration = JsonConfiguration(SnakeCase)

  implicit val carReads: Reads[Car] = (
    (JsPath \ "id").readWithDefault[Long](0) and
      (JsPath \ "registration_number").read[String] and
      (JsPath \ "make").readNullable[String] and
      (JsPath \ "model").readNullable[String] and
      (JsPath \ "color").readNullable[String] and
      (JsPath \ "year").readNullable[Int]
  )(Car.apply _)

  implicit val carWrites: OWrites[Car] = Json.writes[Car]
}