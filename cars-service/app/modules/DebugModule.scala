package modules

import com.google.inject.AbstractModule
import persistence.inmemory.InMemoryCarRepository
import play.api.{Configuration, Environment}
import repositories.{CarRepository, CarStatisticsRepository}

class DebugModule (environment: Environment, configuration: Configuration)
  extends AbstractModule {
    override def configure(): Unit = {
      bind(classOf[CarRepository])
        .to(classOf[InMemoryCarRepository])
      bind(classOf[CarStatisticsRepository])
        .to(classOf[InMemoryCarRepository])
    }
  }
