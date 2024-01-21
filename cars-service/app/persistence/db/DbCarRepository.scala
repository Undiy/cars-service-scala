package persistence.db

import models.{Car, CarStatistics}
import repositories.{CarRepository, CarStatisticsRepository}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DbCarRepository @Inject() (private val carDAO: CarDAO)(implicit executionContext: ExecutionContext) extends
  CarRepository {

  override def getById(id: Long): Future[Option[Car]] = carDAO.getById(id)

  override def add(car: Car): Future[Long] = carDAO.add(car)

  override def update(car: Car): Future[Boolean] = carDAO.update(car).map(_ == 1)

  override def delete(id: Long): Future[Boolean] = carDAO.delete(id).map(_ == 1)

  override def getAll: Future[Seq[Car]] = carDAO.getAll
}
