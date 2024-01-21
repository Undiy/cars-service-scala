package modules

import com.google.inject.AbstractModule
import play.api.{Configuration, Environment}
import repositories.{CarRepository, InMemoryCarRepository}

class DebugModule (environment: Environment, configuration: Configuration)
  extends AbstractModule {
    override def configure(): Unit = {
      bind(classOf[CarRepository])
        .to(classOf[InMemoryCarRepository])
    }
  }
