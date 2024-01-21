package repositories

import models.Car
import repositories.CarField.CarField

import scala.concurrent.Future

trait CarRepository {

  def getById(id: Long): Future[Option[Car]]
  def add(car: Car): Future[Long]
  def update(car: Car): Future[Boolean]
  def delete(id: Long): Future[Boolean]
  def getAll(filters: Map[CarField, String], sort: Option[CarField], desc: Boolean): Future[Seq[Car]]
}

object CarField extends Enumeration {
  type CarField = Value
  val Id = Value
  val RegistrationNumber = Value
  val Make = Value
  val Model = Value
  val Color = Value
  val ManufacturingYear = Value
}