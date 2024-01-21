package modules

import com.google.inject.AbstractModule
import persistence.DbCarRepository
import persistence.bootstrap.DbSchemaInitializer
import play.api.{Configuration, Environment}
import repositories.CarRepository

class DbModule (environment: Environment, configuration: Configuration)
  extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[DbSchemaInitializer]).asEagerSingleton()
    bind(classOf[CarRepository])
      .to(classOf[DbCarRepository])
  }
}
