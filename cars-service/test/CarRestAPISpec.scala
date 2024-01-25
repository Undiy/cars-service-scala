import fake.FakeCars.fakeCars
import models.Car
import models.CarFormat._
import org.specs2.matcher.JsonMatchers
import persistence.db.CarDAO
import play.api.Application
import play.api.libs.json.{JsArray, JsObject, Json}
import play.api.test.{FakeRequest, PlaySpecification, WithApplication}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class CarRestAPISpec extends PlaySpecification with JsonMatchers {
  "Car REST API" should {
    "send 200 & empty JsonArray for '/cars' endpoint when there are no cars" in new WithApplication {
      val result = route(app, FakeRequest(GET, "/cars")).get
      status(result) mustEqual OK
      contentType(result) must beSome("application/json")
      contentAsString(result) must beEqualTo("[]")
    }
    "send 200 & all car objects for '/cars' endpoint" in new WithApplication {
      val app2dao = Application.instanceCache[CarDAO]
      val dao = app2dao(app)

      val insertedIds = Await.result(Future.sequence(fakeCars.map(dao.add)), 1.seconds)

      val result = route(app, FakeRequest(GET, "/cars")).get
      status(result) mustEqual OK
      contentType(result) must beSome("application/json")
      contentAsJson(result).as[JsArray].value.map(Json.fromJson[Car](_).getOrElse(Car(0, ""))).toSet mustEqual
          fakeCars.lazyZip(insertedIds).map((car, id) => car.copy(id = id)).toSet
    }
    "send 200 & filtered car objects for '/cars' endpoint with filter parameter" in new WithApplication {
      val app2dao = Application.instanceCache[CarDAO]
      val dao = app2dao(app)

      Await.ready(Future.sequence(fakeCars.map(dao.add)), 1.seconds)

      val result = route(app, FakeRequest(GET, "/cars?color=green")).get
      status(result) mustEqual OK
      contentType(result) must beSome("application/json")
      forall(contentAsJson(result).as[JsArray].value.map(Json.fromJson[Car](_).getOrElse(Car(0, ""))))(
        _.color must beSome("Green")
      )
    }
    "send 200 & sorted car objects for '/cars' endpoint with sort parameter" in new WithApplication {
      val app2dao = Application.instanceCache[CarDAO]
      val dao = app2dao(app)

      Await.ready(Future.sequence(fakeCars.map(dao.add)), 1.seconds)

      val result = route(app, FakeRequest(GET, "/cars?sort=manufacturing_year")).get
      status(result) mustEqual OK
      contentType(result) must beSome("application/json")
      contentAsJson(result).as[JsArray].value.map(Json.fromJson[Car](_).getOrElse(Car(0, ""))).toSeq must beSorted(Ordering.by[Car, Int]({
        _.manufacturingYear.getOrElse(Int.MaxValue)
      }))
    }
    "send 400 for '/cars' endpoint with sort invalid sort parameter" in new WithApplication {
      val invalidSortParameter = "foo"
      val result = route(app, FakeRequest(GET, s"/cars?sort=$invalidSortParameter")).get
      status(result) mustEqual BAD_REQUEST
      contentType(result) must beSome("application/json")
      contentAsString(result) mustEqual s"""{"error":"Invalid sort parameter: $invalidSortParameter"}"""
    }

    "send 204 & update a car for PUT '/car' endpoint" in new WithApplication {
      val app2dao = Application.instanceCache[CarDAO]
      val dao = app2dao(app)

      val insertedId = Await.result(dao.add(fakeCars(0)), 1.seconds)
      val updatedCar = fakeCars(2).copy(id = insertedId)

      val result = route(app, FakeRequest(PUT, "/car")
        .withBody(Json.toJson(updatedCar))).get
      status(result) mustEqual NO_CONTENT

      val storedCar = Await.result(dao.getById(updatedCar.id), 1.second)

      storedCar must beSome(updatedCar)
    }
    "send 400 for PUT '/car' endpoint on invalid car object in body" in new WithApplication {
      val result = route(app, FakeRequest(PUT, "/car")
        .withBody(JsObject(Seq()))).get
      status(result) mustEqual BAD_REQUEST
    }
    "send 400 for PUT '/car' endpoint on duplicate registration_number" in new WithApplication {
      val app2dao = Application.instanceCache[CarDAO]
      val dao = app2dao(app)

      Await.ready(dao.add(fakeCars(0)), 1.seconds)
      val insertedId = Await.result(dao.add(fakeCars(1)), 1.seconds)

      val result = route(app, FakeRequest(PUT, "/car")
        .withBody(Json.toJson(fakeCars(1).copy(id = insertedId, registrationNumber = fakeCars(0).registrationNumber)))).get
      status(result) mustEqual BAD_REQUEST
      contentType(result) must beSome("application/json")
      contentAsString(result) mustEqual s"""{"error":"Car with registration number `${fakeCars(0).registrationNumber}` already exists"}"""
    }
    "send 404 for PUT '/car' endpoint when no car found for given id" in new WithApplication {
      val result = route(app, FakeRequest(PUT, "/car")
        .withBody(Json.toJson(fakeCars(0)))).get
      status(result) mustEqual NOT_FOUND
    }

    "send 204 & delete a car for DELETE '/car' endpoint" in new WithApplication {
      val app2dao = Application.instanceCache[CarDAO]
      val dao = app2dao(app)

      val insertedId = Await.result(dao.add(fakeCars(0)), 1.seconds)

      val result = route(app, FakeRequest(DELETE, s"/car/$insertedId")).get
      status(result) mustEqual NO_CONTENT

      val storedCars = Await.result(dao.getAll(), 1.second)

      storedCars mustEqual Seq()
    }
    "send 404 for DELETE '/car' endpoint when no car found for given id" in new WithApplication {
      val result = route(app, FakeRequest(DELETE, "/car/1")).get
      status(result) mustEqual NOT_FOUND
    }

    "send 201 & inserted Id for a car for POST '/car' endpoint" in new WithApplication {
      val app2dao = Application.instanceCache[CarDAO]
      val dao = app2dao(app)

      val result = route(app, FakeRequest(POST, "/car")
        .withBody(Json.toJson(fakeCars(0)))).get
      status(result) mustEqual CREATED

      val storedCars = Await.result(dao.getAll(), 1.second)
      contentAsString(result) mustEqual storedCars.head.id.toString
    }
    "send 400 for POST '/car' endpoint on duplicate registration_number" in new WithApplication {
      val app2dao = Application.instanceCache[CarDAO]
      val dao = app2dao(app)

      Await.ready(dao.add(fakeCars(0)), 1.seconds)

      val result = route(app, FakeRequest(POST, "/car")
        .withBody(Json.toJson(fakeCars(0)))).get
      status(result) mustEqual BAD_REQUEST
      contentType(result) must beSome("application/json")
      contentAsString(result) mustEqual s"""{"error":"Car with registration number `${fakeCars(0).registrationNumber}` already exists"}"""
    }

    "send 200 & empty statistics for '/cars/statistics' endpoint when there are no cars" in new WithApplication {
      val result = route(app, FakeRequest(GET, "/cars/statistics")).get
      status(result) mustEqual OK
      contentType(result) must beSome("application/json")
      contentAsString(result) mustEqual """{"num_records":0}"""
    }
    "send 200 & correct statistics '/cars/statistics' endpoint" in new WithApplication {
      val app2dao = Application.instanceCache[CarDAO]
      val dao = app2dao(app)

      Await.ready(Future.sequence(fakeCars.map(dao.add)), 1.seconds)

      val result = route(app, FakeRequest(GET, "/cars/statistics")).get
      status(result) mustEqual OK
      contentType(result) must beSome("application/json")
      contentAsString(result) must /("num_records" -> 6).and(
        /("most_common_color" -> "Green")
      ).and(
        /("most_common_make" -> contain("Kia").and(contain("Toyota")))
      )
    }
  }
}
