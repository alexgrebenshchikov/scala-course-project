package ru.man.power.wirings

import cats.effect.IO
import com.typesafe.config.{Config, ConfigFactory}
import org.asynchttpclient.util.HttpConstants.Methods
import ru.man.power.client.model.configuration.FlightDestinationsClientConfiguration
import ru.man.power.client.model.response.FlightDestinationsResponse
import ru.man.power.commons.configuration.RetryConfiguration
import ru.man.power.utils.TestData.flightDestinationsResponseString
import ru.man.power.utils.TestUtils
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client3.{Response, SttpBackend}
import sttp.model.StatusCode

trait DefaultWirings extends TestUtils {
  val config: Config = ConfigFactory.load()
  val flightDestinationsClientConfiguration: FlightDestinationsClientConfiguration =
    FlightDestinationsClientConfiguration.loadTestConfig(config)
  val retryConfiguration: RetryConfiguration = RetryConfiguration.load(config)
  val sttpBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend
    .stub[IO]
    .whenRequestMatchesPartial {
      case r if r.method.toString() == Methods.GET =>
        Response.ok(
          IO.pure(
            parseAsJsonUnsafe(flightDestinationsResponseString)
              .as[FlightDestinationsResponse]
          ),
        )
      case _ => Response("Not found", StatusCode.BadGateway)
    }
}
