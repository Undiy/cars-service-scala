package controllers

import models.Car
import models.CarFormat._
import models.CarStatisticsFormat._
import play.api.libs.json.{JsNumber, JsObject, JsString, Json}
import play.api.mvc.{BaseController, ControllerComponents}
import repositories.CarField.{CarField, Color, Id, Make, ManufacturingYear, Model, RegistrationNumber}
import repositories.{CarRepository, CarStatisticsRepository}

import java.sql.SQLException
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
  private def onError(ex: Throwable) = InternalServerError(makeError(s"${ex.getClass}: $ex"))

  private def parseSortParameter(sort: Option[String]): Option[Option[CarField]] = sort match {
    case None => Some(None)
    case Some("id") => Some(Some(Id))
    case Some("registration_number") => Some(Some(RegistrationNumber))
    case Some("make") => Some(Some(Make))
    case Some("model") => Some(Some(Model))
    case Some("color") => Some(Some(Color))
    case Some("manufacturing_year") => Some(Some(ManufacturingYear))
    case _ => None
  }
  private def parseFilterParameters(queryString: Map[String, Seq[String]]): Map[CarField, String] = queryString
    .foldLeft(Map[CarField, String]()) { (acc, queryParam) =>
      val (param, value) = queryParam
      param match {
        case "id" => acc + (Id -> value.mkString)
        case "registration_number" => acc + (RegistrationNumber -> value.mkString)
        case "make" => acc + (Make -> value.mkString)
        case "model" => acc + (Model -> value.mkString)
        case "color" => acc + (Color -> value.mkString)
        case "manufacturing_year" => acc + (ManufacturingYear -> value.mkString)
        case _ => acc
      }
    }
  def getAll(sort: Option[String], desc: Option[Boolean]) = Action.async { implicit request =>
    parseSortParameter(sort) match {
      case Some(sortField) => carRepository.getAll(
          parseFilterParameters(request.queryString),
          sortField,
          desc.getOrElse(false)
        )
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
            case ex: SQLException => if (ex.getMessage.contains("ERROR: duplicate key value violates unique constraint")) {
              BadRequest(makeError(s"Car with registration number `${newCar.registrationNumber}` already exists"))
            } else {
              onError(ex)
            }
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
          case ex: SQLException => if (ex.getMessage.contains("ERROR: duplicate key value violates unique constraint")) {
            BadRequest(makeError(s"Car with registration number `${updatedCar.registrationNumber}` already exists"))
          } else {
            onError(ex)
          }
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
