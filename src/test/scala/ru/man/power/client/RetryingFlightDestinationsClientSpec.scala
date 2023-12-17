package ru.man.power.client

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import com.dimafeng.testcontainers.{ContainerDef, DockerComposeContainer, ExposedService}
import org.asynchttpclient.DefaultAsyncHttpClient
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.testcontainers.containers.wait.strategy.Wait
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jFactory
import ru.man.power.client.model.response.FlightDestinationsResponse
import ru.man.power.commons.RetryUtilsImpl
import ru.man.power.utils.TestData
import ru.man.power.utils.TestData.dataList
import ru.man.power.wirings.DefaultWirings
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

import java.io.File

class RetryingFlightDestinationsClientSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with OrderServiceImplSpecUtils
    with TestContainerForAll {

  override val containerDef: ContainerDef = DockerComposeContainer.Def(
    new File("src/test/resources/order/docker-compose.yml"),
    tailChildContainers = true,
    exposedServices = Seq(
      ExposedService("wiremock", 8080, Wait.forListeningPort()),
    ),
  )

  "findFlightDestinations" - {
    "find flights destinations by params" in {
      service
        .findFlightDestinations(TestData.searchParams, "token")
        .asserting(_ shouldBe Right(FlightDestinationsResponse(dataList, None, None, None)))
    }
  }

}

trait OrderServiceImplSpecUtils extends DefaultWirings {
  val backend: SttpBackend[IO, Any] =
    AsyncHttpClientCatsBackend.usingClient[IO](new DefaultAsyncHttpClient())
  val client: HttpFlightDestinationsClient[IO] = new HttpFlightDestinationsClient[IO](backend, flightDestinationsClientConfiguration)
  val logger: Logger[IO] = Slf4jFactory.create[IO].getLogger
  val retryUtils: RetryUtilsImpl[IO] = new RetryUtilsImpl[IO](logger, retryConfiguration)
  val service: RetryingFlightDestinationsClient[IO] = new RetryingFlightDestinationsClient[IO](client, retryUtils)
}
