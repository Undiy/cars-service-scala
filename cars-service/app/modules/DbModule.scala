package modules

import com.google.inject.AbstractModule
import persistence.db.{DbCarRepository, DbCarStatisticsRepository}
import persistence.db.bootstrap.DbSchemaInitializer
import play.api.{Configuration, Environment}
import repositories.{CarRepository, CarStatisticsRepository}

class DbModule (environment: Environment, configuration: Configuration)
  extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[DbSchemaInitializer]).asEagerSingleton()
    bind(classOf[CarRepository])
      .to(classOf[DbCarRepository])
    bind(classOf[CarStatisticsRepository])
      .to(classOf[DbCarStatisticsRepository])
  }
}
