package controllers

import models.Car
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import javax.inject.Inject
import javax.inject.Singleton
import scala.collection.mutable.ListBuffer

@Singleton
class CarsController @Inject()(val controllerComponents: ControllerComponents)
  extends BaseController {

  private val carsList = new ListBuffer[Car]()
  carsList += Car(1, "TEST_123")
  carsList += Car(2, "TEST_123", make = Some("foo"), color = Some("green"), manufacturingYear = Some(2023))

  implicit val carJson = Json.format[Car]

  def getAll = Action {
    Ok(Json.toJson(carsList))
  }

  def getById(id: Long) = Action {
    carsList.find(_.id == id) match {
      case Some(item) => Ok(Json.toJson(item))
      case None => NotFound
    }
  }
}
