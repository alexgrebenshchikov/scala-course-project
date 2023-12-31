package ru.man.power.commons.configuration

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus.toFicusConfig
import net.ceedubs.ficus.readers.ValueReader

import scala.concurrent.duration.{FiniteDuration, SECONDS}

case class RetryConfiguration(retryDuration: FiniteDuration, amount: Int)

object RetryConfiguration {
  def load(config: Config): RetryConfiguration =
    config.as[RetryConfiguration]("client")

  private implicit val petClientConfigurationReader: ValueReader[RetryConfiguration] =
    ValueReader.relative(config =>
      RetryConfiguration(
        retryDuration = FiniteDuration.apply(config.getLong("retry-duration"), SECONDS),
        amount = config.getInt("amount"),
      ),
    )
}
