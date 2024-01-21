package controllers

import models.Car
import models.CarFormat._
import play.api.libs.json.{JsNumber, JsObject, JsString, Json}
import play.api.mvc.{BaseController, ControllerComponents}
import repositories.CarRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class CarsController @Inject() (
                      val carRepository: CarRepository,
                      val controllerComponents: ControllerComponents
                    )(implicit executionContext: ExecutionContext)
  extends BaseController {

  private def onError(ex: Throwable) = InternalServerError(JsObject(Map(
      ("error" -> JsString(ex.toString))
    )))

  def getAll = Action.async {
    carRepository.getAll()
      .map(cars => Ok(Json.toJson(cars)))
      .recover(onError(_))
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
          .recover(onError(_))
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
        .recover(onError(_))
      case None =>
        Future(BadRequest)
    }
  }

  def deleteById(id: Long) = Action.async {
    carRepository.delete(id)
      .map(if (_) NoContent else NotFound)
      .recover(onError(_))
  }
}
