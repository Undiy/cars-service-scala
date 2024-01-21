package persistence.bootstrap

import persistence.CarDAO

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class DbSchemaInitializer @Inject() (carDAO: CarDAO)(implicit executionContext: ExecutionContext) {
  carDAO.initSchema()
}
