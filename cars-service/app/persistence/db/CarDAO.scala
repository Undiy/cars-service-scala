package persistence.db

import models.{Car, CarStatistics, CarStatisticsTimestamps}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repositories.CarSort.{CarSort, Color, Id, Make, ManufacturingYear, Model, NoSort, RegistrationNumber}
import slick.jdbc.JdbcProfile

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import slick.lifted.ColumnOrdered

class CarDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val cars = TableQuery[CarsTable]

  def initSchema(): Future[Unit] = db.run(cars.schema.createIfNotExists)

  def getById(id: Long): Future[Option[Car]] = db.run(cars.filter(_.id === id).result.headOption.map(_.map(_.toCar)))

  def add(car: Car): Future[Long] = db.run(cars returning cars.map(_.id) += carDTOFromCar(car))

  def update(car: Car): Future[Int] = db.run(cars
    .filter(_.id === car.id)
    .map(v => (
      v.registrationNumber,
      v.make,
      v.model,
      v.color,
      v.manufacturingYear,
      v.updatedAt
    ))
    .update((
      car.registrationNumber,
      car.make,
      car.model,
      car.color,
      car.manufacturingYear,
      LocalDateTime.now()
    )))

  def delete(id: Long): Future[Int] = db.run(cars.filter(_.id === id).delete)

  def getAll(carSort: CarSort, desc: Boolean): Future[Seq[Car]] = {

    def sort[T](column: ColumnOrdered[T]) = if (desc) column.desc.nullsFirst else column.asc.nullsLast

   db.run(
      (carSort match {
        case NoSort => cars.result
        case Id => cars.sortBy(t => sort(t.id)).result
        case RegistrationNumber => cars.sortBy(t => sort(t.registrationNumber)).result
        case Make => cars.sortBy(t => sort(t.make)).result
        case Model => cars.sortBy(t => sort(t.model)).result
        case Color => cars.sortBy(t => sort(t.color)).result
        case ManufacturingYear => cars.sortBy(t => sort(t.manufacturingYear)).result
      }).map(_.map(_.toCar))
    )
  }

  def getStatistics: Future[CarStatistics] = {

    for {
      numRecords <- db.run((cars.length.result))
      timestamps <- db.run(DBIO.sequence(Seq(
        cars.map(_.createdAt).min.result,
        cars.map(_.createdAt).max.result,
        cars.filterNot(q => q.updatedAt === q.createdAt).map(_.updatedAt).min.result,
        cars.filterNot(q => q.updatedAt === q.createdAt).map(_.updatedAt).max.result
      )))
      colors <- db.run(mostCommonQuery(_.color.getOrElse("")))
      makes <- db.run(mostCommonQuery(_.make.getOrElse("")))
    } yield {
      val (createdAt, updatedAt) = timestamps.toList match {
        case List(createdAtMin, createdAtMax, updatedAtMin, updatedAtMax) => (
          createdAtMin.flatMap(first => createdAtMax.map(latest => CarStatisticsTimestamps(first, latest))),
          updatedAtMin.flatMap(first => updatedAtMax.map(latest => CarStatisticsTimestamps(first, latest)))
          )
        case _ => (None, None)
      }

      CarStatistics(
        numRecords,
        createdAt,
        updatedAt,
        mostCommonJoin(colors),
        mostCommonJoin(makes)
      )
    }
  }

  private def mostCommonQuery(selector: CarsTable => Rep[String]) = cars
    .groupBy(selector)
    .map {
      case (value, group) => (value, group.length)
    }
    .sortBy {
      case (_, n) => n.desc
    }
    .result

  private def mostCommonJoin(result: Seq[(String, Int)]) = {
    result.headOption.map {
      case (_, n) => result.takeWhile(_._2 == n).map(_._1).mkString(", ")
    }
  }

  private case class CarDTO(
                             id: Long,
                             registrationNumber: String,
                             make: Option[String] = None,
                             model: Option[String] = None,
                             color: Option[String] = None,
                             manufacturingYear: Option[Int] = None,
                             createdAt: LocalDateTime,
                             updatedAt: LocalDateTime
                           ) {
    def toCar: Car = Car(id, registrationNumber, make, model, color, manufacturingYear)
  }

    private def carDTOFromCar(
                               car: Car,
                               createdAt: LocalDateTime = LocalDateTime.now()
                             ): CarDTO = CarDTO(
      car.id,
      car.registrationNumber,
      car.make,
      car.model,
      car.color,
      car.manufacturingYear,
      createdAt,
      createdAt
    )

  private class CarsTable(tag: Tag) extends Table[CarDTO](tag, "car") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def registrationNumber = column[String]("registration_number", O.Unique)
    def make = column[Option[String]]("make")
    def model = column[Option[String]]("model")
    def color = column[Option[String]]("color")
    def manufacturingYear = column[Option[Int]]("manufacturing_year")

    def createdAt = column[LocalDateTime]("created_at")
    def updatedAt = column[LocalDateTime]("updated_at")


    def * = (id, registrationNumber, make, model, color, manufacturingYear, createdAt, updatedAt) <>
      (CarDTO.tupled, CarDTO.unapply)
  }
}
