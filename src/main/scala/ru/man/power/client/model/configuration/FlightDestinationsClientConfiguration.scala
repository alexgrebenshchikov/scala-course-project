package ru.man.power.client.model.configuration

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus.toFicusConfig
import net.ceedubs.ficus.readers.ValueReader

import scala.concurrent.duration.{FiniteDuration, SECONDS}

case class FlightDestinationsClientConfiguration(
    baseUrl: String,
    renewTokenUrl: String,
    clientId: String,
    clientSecret: String,
    timeout: FiniteDuration,
)

object FlightDestinationsClientConfiguration {
  def loadConfig(config: Config): FlightDestinationsClientConfiguration =
    config.as[FlightDestinationsClientConfiguration]("amadeus-api.find")

  def loadTestConfig(config: Config): FlightDestinationsClientConfiguration =
    config.as[FlightDestinationsClientConfiguration]("amadeus-api-test.find")


  private implicit val flightDestinationsClientConfigurationReader
      : ValueReader[FlightDestinationsClientConfiguration] =
    ValueReader.relative(config =>
      FlightDestinationsClientConfiguration(
        baseUrl = config.getString("base-url"),
        renewTokenUrl = config.getString("renew-token-url"),
        clientId = config.getString("client-id"),
        clientSecret = config.getString("client-secret"),
        timeout = FiniteDuration.apply(config.getLong("timeout"), SECONDS),
      ),
    )
}
