package ru.man.power.wirings

import cats.effect.IO
import com.typesafe.config.{Config, ConfigFactory}
import org.asynchttpclient.util.HttpConstants.Methods
import ru.man.power.client.model.configuration.OrderClientConfiguration
import ru.man.power.client.model.response.OrderResponse
import ru.man.power.commons.configuration.RetryConfiguration
import ru.man.power.utils.TestData.orderResponseString
import ru.man.power.utils.TestUtils
import ru.man.power.client.model.response
import sttp.client3.{Response, SttpBackend}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.model.StatusCode

trait DefaultWirings extends TestUtils {
  val config: Config = ConfigFactory.load()
  val orderClientConfiguration: OrderClientConfiguration = OrderClientConfiguration.load(config)
  val retryConfiguration: RetryConfiguration = RetryConfiguration.load(config)
  val sttpBackend: SttpBackend[IO, Any] = AsyncHttpClientCatsBackend
    .stub[IO]
    .whenRequestMatchesPartial {
      case r if r.uri.toString().contains("api/v1/order") && r.method.toString() == Methods.POST =>
        Response.ok(
          IO.pure(
            parseAsJsonUnsafe(orderResponseString)
              .as[OrderResponse]
              .fold(_.toString(), response => response),
          ),
        )
      case r if r.uri.toString().contains(s"api/v1/order") && r.method.toString() == Methods.GET =>
        Response.ok(
          IO.pure(
            parseAsJsonUnsafe(orderResponseString)
              .as[Option[OrderResponse]]
              .fold(_.toString(), response => response),
          ),
        )
      case _ => Response("Not found", StatusCode.BadGateway)
    }
}
