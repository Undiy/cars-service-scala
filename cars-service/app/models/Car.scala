package models

case class Car(
  id: Long,
  registrationNumber: String,
  make: Option[String] = None,
  model: Option[String] = None,
  color: Option[String] = None,
  manufacturingYear: Option[Int] = None
)
