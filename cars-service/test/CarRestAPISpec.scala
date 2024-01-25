import fake.FakeCars.fakeCars
import org.specs2.matcher.JsonMatchers
import org.specs2.matcher.JsonMatchers.anyValue.^^
import org.specs2.mutable._
import persistence.db.CarDAO
import play.api.Application
import play.api.test.{FakeRequest, PlaySpecification, WithApplication}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class CarRestAPISpec extends PlaySpecification with JsonMatchers {
  "Car REST API" should {
    "send 200 & empty statistics when there are no cars" in new WithApplication {
      val result = route(app, FakeRequest(GET, "/cars/statistics")).get
      status(result) mustEqual OK
      contentType(result) must beSome("application/json")
      contentAsString(result) must beEqualTo("""{"num_records":0}""")
    }
  }

  "Car REST API" should {
    "send 200 & correct statistics for several cars" in new WithApplication {
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
