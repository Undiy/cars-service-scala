package repositories
import com.google.inject.Singleton
import models.Car

import scala.collection.mutable.ListBuffer

@Singleton
class InMemoryCarRepository extends CarRepository {
  private val cars = new ListBuffer[Car]()

  private def getNextId(): Long = cars.map(_.id).maxOption.getOrElse(0L) + 1

  override def getById(id: Long): Option[Car] = cars.find(_.id == id)

  override def add(car: Car): Long = {
    val nextId = getNextId()
    cars += car.copy(id = nextId)
    nextId
  }

  override def update(car: Car): Boolean = {
    val i = cars.indexWhere(_.id == car.id)
    if (i >= 0) {
      cars(i) = car
      true
    } else {
      false
    }
  }

  override def delete(id: Long): Boolean = {
    val i = cars.indexWhere(_.id == id)
    if (i >= 0) {
      cars.remove(i)
      true
    } else {
      false
    }

  }

  override def findAll(): List[Car] = cars.toList
}
