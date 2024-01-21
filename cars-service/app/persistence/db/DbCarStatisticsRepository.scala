package persistence.db

import models.CarStatistics
import repositories.CarStatisticsRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DbCarStatisticsRepository  @Inject() (private val carDAO: CarDAO)(implicit executionContext: ExecutionContext)
  extends CarStatisticsRepository {

  override def getStatistics: Future[CarStatistics] = carDAO.getStatistics
}
