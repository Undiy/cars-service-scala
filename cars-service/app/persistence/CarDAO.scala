package persistence

import models.Car

import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class CarDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val cars = TableQuery[CarsTable]

  def initSchema() = db.run(cars.schema.createIfNotExists)

  def getById(id: Long): Future[Option[Car]] = db.run(cars.filter(_.id === id).result.headOption)
  def add(car: Car): Future[Long] = db.run(cars returning cars.map(_.id) += car)
  def update(car: Car): Future[Int] = db.run(cars
    .filter(_.id === car.id)
    .update(car))
  def delete(id: Long): Future[Int] = db.run(cars.filter(_.id === id).delete)
  def getAll(): Future[Seq[Car]] = db.run(cars.result)

  private class CarsTable(tag: Tag) extends Table[Car](tag, "car") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def registrationNumber = column[String]("registration_number", O.Unique)
    def make = column[Option[String]]("make")
    def model = column[Option[String]]("model")
    def color = column[Option[String]]("color")
    def manufacturingYear = column[Option[Int]]("manufacturing_year")

    def * = (id, registrationNumber, make, model, color, manufacturingYear) <>
      (Car.tupled, Car.unapply)
  }
}
