package controllers

import models.Car
import models.CarFormat._
import models.CarStatisticsFormat._
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import play.api.libs.json.{JsNumber, JsObject, JsString, Json}
import play.api.mvc.{BaseController, ControllerComponents}
import repositories.CarSort.{CarSort, Color, Id, Make, ManufacturingYear, Model, NoSort, RegistrationNumber}
import repositories.{CarRepository, CarStatisticsRepository}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class CarsController @Inject() (
                      val carRepository: CarRepository,
                      val carStatisticsRepository: CarStatisticsRepository,
                      val controllerComponents: ControllerComponents
                    )(implicit executionContext: ExecutionContext)
  extends BaseController {

  private def makeError(message: String) = JsObject(Map(
    "error" -> JsString(message)
  ))
  private def onError(ex: Throwable) = InternalServerError(makeError(s"${ex.getClass}: ${ex}"))

  private def parseSortParameter(sort: Option[String]): Option[CarSort] = sort match {
    case None => Some(NoSort)
    case Some("id") => Some(Id)
    case Some("registration_number") => Some(RegistrationNumber)
    case Some("make") => Some(Make)
    case Some("model") => Some(Model)
    case Some("color") => Some(Color)
    case Some("manufacturing_year") => Some(ManufacturingYear)
    case _ => None
  }
  def getAll(sort: Option[String]) = Action.async {
    parseSortParameter(sort) match {
      case Some(carSort) => carRepository.getAll(carSort)
        .map(cars => Ok(Json.toJson(cars)))
        .recover(onError(_))
      case None => Future {
        BadRequest(makeError(s"Invalid sort parameter: ${sort.get}"))
      }
    }
  }

  def getById(id: Long) = Action.async {
    carRepository.getById(id)
      .map {
        case Some(car) => Ok(Json.toJson(car))
        case None => NotFound
      }
      .recover(onError(_))
  }

  def add = Action.async { implicit request =>
    request.body.asJson flatMap { json =>
      // explicitly set the "id" field to make generated parser happy
      (json.as[JsObject] + ("id" -> JsNumber(0))).asOpt[Car]
    } match {
      case Some(newCar) =>
        carRepository.add(newCar)
          .map(newId => Created(Json.toJson(newId)))
          .recover {
            case _: JdbcSQLIntegrityConstraintViolationException => BadRequest(makeError(
              s"Car with registration number `${newCar.registrationNumber}` already exists"))
            case ex => onError(ex)
          }
      case None =>
        Future(BadRequest)
    }
  }

  def update = Action.async { implicit request =>
    request.body.asJson flatMap {
      Json.fromJson[Car](_).asOpt
    } match {
      case Some(updatedCar) => carRepository.update(updatedCar)
        .map(if (_) NoContent else NotFound)
        .recover {
          case _: JdbcSQLIntegrityConstraintViolationException => BadRequest(makeError(
            s"Car with registration number `${updatedCar.registrationNumber}` already exists"))
          case ex => onError(ex)
        }
      case None =>
        Future(BadRequest)
    }
  }

  def deleteById(id: Long) = Action.async {
    carRepository.delete(id)
      .map(if (_) NoContent else NotFound)
      .recover(onError(_))
  }

  def getStatistics = Action.async {
    carStatisticsRepository.getStatistics
      .map( carStatistics => Ok(Json.toJson(carStatistics) ))
      .recover(onError(_))
  }
}
