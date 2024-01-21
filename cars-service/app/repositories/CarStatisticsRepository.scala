package repositories

import models.CarStatistics

import scala.concurrent.Future

trait CarStatisticsRepository {

  def getStatistics: Future[CarStatistics]
}
