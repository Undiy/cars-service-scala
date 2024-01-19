package controllers

import models.Car
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, BaseController, ControllerComponents, Request}

import javax.inject.{Inject, Singleton}
import scala.collection.mutable.ListBuffer

@Singleton
class CarsController @Inject()(val controllerComponents: ControllerComponents)
  extends BaseController {

  private val carsList = new ListBuffer[Car]()
  carsList += Car(1, "TEST_123")
  carsList += Car(2, "TEST_123", make = Some("foo"), color = Some("green"), manufacturingYear = Some(2023))

  def getAll = Action {
    Ok(Json.toJson(carsList))
  }

  def getById(id: Long) = Action {
    carsList.find(_.id == id) match {
      case Some(item) => Ok(Json.toJson(item))
      case None => NotFound
    }
  }

  private def getCarFromRequest(request: Request[AnyContent]) = request.body.asJson.flatMap(
    Json.fromJson[Car](_).asOpt
  )

  def add = Action { implicit request =>
    getCarFromRequest(request) match {
      case Some(newCar) =>
        val nextId = carsList.map(_.id).max + 1
        val toBeAdded = newCar.copy(id = nextId)
        carsList += toBeAdded
        Created(Json.toJson(nextId))
      case None =>
        BadRequest
    }
  }

  def update = Action { implicit request =>
    getCarFromRequest(request) flatMap { updatedCar: Car =>
      val i = carsList.indexWhere(_.id == updatedCar.id)
      if (i >= 0) Some((updatedCar, i)) else None
    } match {
      case Some((updatedCar, i)) =>
        carsList(i) = updatedCar
        NoContent
      case None =>
        BadRequest
    }
  }

  def deleteById(id: Long) = Action {
    val i = carsList.indexWhere(_.id == id)
    if (i >= 0) {
      carsList.remove(i)
      NoContent
    } else {
      BadRequest
    }
  }
}
