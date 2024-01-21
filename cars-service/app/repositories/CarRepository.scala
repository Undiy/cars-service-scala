package repositories

import models.Car

import scala.concurrent.Future

trait CarRepository {
  
  def getById(id: Long): Future[Option[Car]]
  def add(car: Car): Future[Long]
  def update(car: Car): Future[Boolean]
  def delete(id: Long): Future[Boolean]
  def getAll(): Future[Seq[Car]]
}
