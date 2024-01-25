package persistence.db.bootstrap

import persistence.db.CarDAO

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext}

class DbSchemaInitializer @Inject() (carDAO: CarDAO)(implicit executionContext: ExecutionContext) {
  Await.ready(carDAO.initSchema(), 30.seconds)
}
