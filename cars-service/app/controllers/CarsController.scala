package controllers

import models.Car
import play.api.libs.json.{JsNumber, JsObject, Json}
import play.api.mvc.{BaseController, ControllerComponents}
import repositories.CarRepository

import javax.inject.Inject

class CarsController @Inject() (
                      val carRepository: CarRepository,
                      val controllerComponents: ControllerComponents
                    )
  extends BaseController {

  def getAll = Action {
    Ok(Json.toJson(carRepository.findAll()))
  }

  def getById(id: Long) = Action {
    carRepository.getById(id) match {
      case Some(item) => Ok(Json.toJson(item))
      case None => NotFound
    }
  }

  def add = Action { implicit request =>
    request.body.asJson flatMap { json =>
      // explicitly set the "id" field to make generated parser happy
      (json.as[JsObject] + ("id" -> JsNumber(0))).asOpt[Car]
    } match {
      case Some(newCar) =>
        val newId = carRepository.add(newCar)
        Created(Json.toJson(newId))
      case None =>
        BadRequest
    }
  }

  def update = Action { implicit request =>
    request.body.asJson flatMap {
      Json.fromJson[Car](_).asOpt
    } match {
      case Some(updatedCar) => if (carRepository.update(updatedCar)) {
        NoContent
      } else {
        NotFound
      }
      case None =>
        BadRequest
    }
  }

  def deleteById(id: Long) = Action {
    if (carRepository.delete(id)) {
      NoContent
    } else {
      NotFound
    }
  }
}
