package repositories

import models.Car
import repositories.CarSort.CarSort

import scala.concurrent.Future

trait CarRepository {

  def getById(id: Long): Future[Option[Car]]
  def add(car: Car): Future[Long]
  def update(car: Car): Future[Boolean]
  def delete(id: Long): Future[Boolean]
  def getAll(sort: CarSort): Future[Seq[Car]]
}

object CarSort extends Enumeration {
  type CarSort = Value
  val NoSort = Value
  val Id = Value
  val RegistrationNumber = Value
  val Make = Value
  val Model = Value
  val Color = Value
  val ManufacturingYear = Value
}
