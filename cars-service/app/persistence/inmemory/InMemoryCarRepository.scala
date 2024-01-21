package persistence.inmemory

import com.google.inject.Singleton
import models.{Car, CarStatistics}
import repositories.CarField.CarField
import repositories.{CarRepository, CarStatisticsRepository}

import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InMemoryCarRepository @Inject() ()(implicit executionContext: ExecutionContext)
  extends CarRepository with CarStatisticsRepository {
  private val cars = new ListBuffer[Car]()

  private def getNextId(): Long = cars.map(_.id).maxOption.getOrElse(0L) + 1

  override def getById(id: Long) = Future {
    cars.find(_.id == id)
  }

  override def add(car: Car) = Future {
    val nextId = getNextId()
    cars += car.copy(id = nextId)
    nextId
  }

  override def update(car: Car) = Future {
    val i = cars.indexWhere(_.id == car.id)
    if (i >= 0) {
      cars(i) = car
      true
    } else {
      false
    }
  }

  override def delete(id: Long) = Future {
    val i = cars.indexWhere(_.id == id)
    if (i >= 0) {
      cars.remove(i)
      true
    } else {
      false
    }

  }

  override def getAll(filters: Map[CarField, String], sort: Option[CarField], desc: Boolean) = Future {
    cars.toList
  }

  override def getStatistics = Future {
    CarStatistics(cars.length)
  }
}
