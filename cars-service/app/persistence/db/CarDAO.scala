package persistence.db

import models.{Car, CarStatistics, CarStatisticsTimestamps}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repositories.CarField.{CarField, Color, Id, Make, ManufacturingYear, Model, RegistrationNumber}
import slick.jdbc.JdbcProfile
import slick.lifted.ColumnOrdered

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

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

  def getAll(filters: Map[CarField, String] = Map(), sort: Option[CarField] = None, desc: Boolean = false): Future[Seq[Car]] = {
    def sortColumn[T](column: ColumnOrdered[T]) = if (desc) column.desc.nullsFirst else column.asc.nullsLast

    val query = filters.foldLeft(cars.distinct) { (acc, filter) =>
      filter match {
        case (field, value) => field match {
          case Id => acc.filter(_.id === value.toLongOption.getOrElse(0L))
          case RegistrationNumber => acc.filter(_.registrationNumber.like(s"%$value%"))
          case Make => acc.filter(_.make.toLowerCase.like(s"%${value.toLowerCase}%"))
          case Model => acc.filter(_.model.toLowerCase.like(s"%${value.toLowerCase}%"))
          case Color => acc.filter(_.color.toLowerCase.like(s"%${value.toLowerCase}%"))
          case ManufacturingYear => acc.filter(_.manufacturingYear === value.toIntOption.getOrElse(0))
        }
      }
    }

    val sortedQuery = sort match {
      case Some(Id) => query.sortBy(t => sortColumn(t.id))
      case Some(RegistrationNumber) => query.sortBy(t => sortColumn(t.registrationNumber))
      case Some(Make) => query.sortBy(t => sortColumn(t.make))
      case Some(Model) => query.sortBy(t => sortColumn(t.model))
      case Some(Color) => query.sortBy(t => sortColumn(t.color))
      case Some(ManufacturingYear) => query.sortBy(t => sortColumn(t.manufacturingYear))
      case _ => query
    }

    db.run(sortedQuery.result.map(_.map(_.toCar)))
  }

  def getStatistics: Future[CarStatistics] = {

    for {
      numRecords <- db.run(cars.length.result)
      timestamps <- db.run(DBIO.sequence(Seq(
        cars.map(_.createdAt).min.result,
        cars.map(_.createdAt).max.result,
        cars.filterNot(q => q.updatedAt === q.createdAt).map(_.updatedAt).min.result,
        cars.filterNot(q => q.updatedAt === q.createdAt).map(_.updatedAt).max.result
      )))
      colors <- db.run(mostCommonQuery(_.color))
      makes <- db.run(mostCommonQuery(_.make))
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

  private def mostCommonQuery(selector: CarsTable => Rep[Option[String]]) = cars
    .filterNot(t => selector(t).isEmpty)
    .groupBy(t => selector(t).getOrElse(""))
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
