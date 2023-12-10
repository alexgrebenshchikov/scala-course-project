package ru.man.power.client

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import com.softwaremill.macwire.wire
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import ru.man.power.client.model.request.CreateOrderRequest
import ru.man.power.client.model.response.OrderResponse
import ru.man.power.wirings.DefaultWirings
import ru.man.power.utils.TestData.{date, id, petId}

class HttpOrderClientSpec
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with OrderClientImplSpecUtils {
  "createOrder" - {
    "create order by id" in {
      client
        .createOrder(CreateOrderRequest(petId))
        .asserting(_ shouldBe OrderResponse(id = id, petId = petId, date = date))
    }
  }
  "findOrder" - {
    "find order by id" in {
      client
        .findOrder(id)
        .asserting(_ shouldBe Some(OrderResponse(id = id, petId = petId, date = date)))
    }
  }
}

trait OrderClientImplSpecUtils extends DefaultWirings {
  val client: HttpOrderClient[IO] = wire[HttpOrderClient[IO]]
}
