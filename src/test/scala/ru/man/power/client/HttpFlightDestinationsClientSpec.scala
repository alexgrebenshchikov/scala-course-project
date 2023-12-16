package ru.man.power.client

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import com.softwaremill.macwire.wire
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import ru.man.power.client.model.response.FlightDestinationsResponse
import ru.man.power.utils.TestData
import ru.man.power.utils.TestData.dataList
import ru.man.power.wirings.DefaultWirings

class HttpFlightDestinationsClientSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with FlightDestinationsClientImplSpecUtils {
  "findFlightDestinations" - {
    "find flights destinations by params" in {
      client
        .findFlightDestinations(TestData.searchParams, "token")
        .asserting(_ shouldBe Right(FlightDestinationsResponse(dataList, None, None, None)))
    }
  }
}

trait FlightDestinationsClientImplSpecUtils extends DefaultWirings {
  val client: HttpFlightDestinationsClient[IO] = wire[HttpFlightDestinationsClient[IO]]
}
