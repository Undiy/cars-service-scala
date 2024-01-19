package repositories

import models.Car

trait CarRepository {
  def getById(id: Long): Option[Car]
  def add(car: Car): Long
  def update(car: Car): Boolean
  def delete(id: Long): Boolean
  def findAll(): List[Car]
}
