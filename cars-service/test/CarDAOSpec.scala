import FakeCars.fakeCars
import models.{Car, CarStatistics}
import org.specs2.mutable.Specification
import persistence.db.CarDAO
import play.api.Application
import play.api.test.WithApplicationLoader
import repositories.CarField

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

private object FakeCars {
  val fakeCars = List(
    Car(0, "number1"),
    Car(0, "number2", make = Some("Toyota"), manufacturingYear = Some(1999)),
    Car(0, "number3", make = Some("Kia"), model = Some("Rio"), color = Some("Black"), manufacturingYear = Some(2010)),
    Car(0, "number4", make = Some("Toyota"), model = Some("Corolla"), color = Some("Green"), manufacturingYear = Some(2001)),
    Car(0, "number5", make = Some("Kia"), model = Some("Soul"), color = Some("White"), manufacturingYear = Some(2010)),
    Car(0, "number6", make = Some("Honda"), model = Some("Civic"), color = Some("Green"), manufacturingYear = Some(2005))
  )
}

class CarDAOSpec extends Specification {
  "CarDAO" should {
    "be able to save and retrieve cars" in new WithApplicationLoader {
      val app2dao = Application.instanceCache[CarDAO]
      val dao: CarDAO = app2dao(app)

      Await.result(dao.initSchema(), 1.seconds)
      val insertedIds = Await.result(Future.sequence(fakeCars.map(dao.add)), 1.seconds)
      val storedCars = Await.result(dao.getAll(), 1.seconds)

      storedCars.toSet must equalTo(fakeCars.lazyZip(insertedIds).map( (car, id) => car.copy(id = id) ).toSet)
    }

    "update a car" in new WithApplicationLoader {
      val app2dao = Application.instanceCache[CarDAO]
      val dao: CarDAO = app2dao(app)


      Await.result(dao.initSchema(), 1.seconds)
      val insertedId = Await.result(dao.add(fakeCars(0)), 1.seconds)
      val updatedCar = fakeCars(2).copy(id = insertedId)

      Await.result(dao.update(updatedCar), 1.seconds)

      val storedCar = Await.result(dao.getById(updatedCar.id), 1.second)

      storedCar must beSome(updatedCar)
    }
  }

  "delete a car" in new WithApplicationLoader {
    val app2dao = Application.instanceCache[CarDAO]
    val dao: CarDAO = app2dao(app)


    Await.result(dao.initSchema(), 1.seconds)
    val insertedId = Await.result(dao.add(fakeCars(0)), 1.seconds)
    val updatedCar = fakeCars(2).copy(id = insertedId)

    Await.result(dao.delete(updatedCar.id), 1.seconds)

    val storedCars = Await.result(dao.getAll(), 1.second)

    storedCars must equalTo(Seq())
  }

  "return empty statistics when there are no cars"  in new WithApplicationLoader {
    val app2dao = Application.instanceCache[CarDAO]
    val dao: CarDAO = app2dao(app)

    val emptyStats = Await.result(dao.getStatistics, 1.second)

    emptyStats must equalTo(CarStatistics(0))
  }

  "return correct statistics for several cars" in new WithApplicationLoader {
    val app2dao = Application.instanceCache[CarDAO]
    val dao: CarDAO = app2dao(app)

    Await.result(dao.initSchema(), 1.seconds)
    Await.result(Future.sequence(fakeCars.map(dao.add)), 1.seconds)

    val stats = Await.result(dao.getStatistics, 1.second)

    stats.numRecords must equalTo(fakeCars.size)
    stats.mostCommonMake.get.split(", ").toSeq must containAllOf(Seq("Toyota", "Kia"))
    stats.mostCommonColor must beSome("Green")
  }

  "filter by field values" in new WithApplicationLoader {
    val app2dao = Application.instanceCache[CarDAO]
    val dao: CarDAO = app2dao(app)

    Await.result(dao.initSchema(), 1.seconds)
    Await.result(Future.sequence(fakeCars.map(dao.add)), 1.seconds)

    val storedCars = Await.result(dao.getAll(Map(
      CarField.Color -> "Green"
    )), 1.second)

    forall(storedCars)(_.color must beSome("Green"))
  }

  "sort by field values" in new WithApplicationLoader {
    val app2dao = Application.instanceCache[CarDAO]
    val dao: CarDAO = app2dao(app)

    Await.result(dao.initSchema(), 1.seconds)
    Await.result(Future.sequence(fakeCars.map(dao.add)), 1.seconds)

    val storedCars = Await.result(dao.getAll(
      sort = Some(CarField.ManufacturingYear)
    ), 1.second)

    storedCars must beSorted(Ordering.by[Car, Int]({
      _.manufacturingYear.getOrElse(Int.MaxValue)
    }))
  }
}
