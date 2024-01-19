package controllers

import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarsController @Inject()(val controllerComponents: ControllerComponents)
  extends BaseController {

  def getAll(): Action[AnyContent] = Action {
    NoContent
  }
}
