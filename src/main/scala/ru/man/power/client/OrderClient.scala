package ru.man.power.client

import cats.effect.{Ref, Sync}
import cats.effect.kernel.Async
import cats.implicits._
import ru.man.power.client.model.configuration.OrderClientConfiguration
import ru.man.power.client.model.request.CreateOrderRequest
import ru.man.power.client.model.response.OrderResponse
import ru.man.power.commons.SttpResponseUtils
import ru.man.power.client.model.response
import sttp.client3.circe._
import sttp.client3.{SttpBackend, UriContext, basicRequest}
import sttp.model.Uri

import java.util.UUID

trait OrderClient[F[_]] {
  def createOrder(request: CreateOrderRequest): F[OrderResponse]
  def findOrder(orderId: UUID): F[Option[OrderResponse]]
}

class HttpOrderClient[F[_]: Async](
    sttpBackend: SttpBackend[F, Any],
    orderClientConfiguration: OrderClientConfiguration,
) extends OrderClient[F] {
  override def createOrder(request: CreateOrderRequest): F[OrderResponse] = {
    val createOrderUrl: Uri =
      uri"${orderClientConfiguration.baseUrl}/api/v1/order?id=${123}"

    basicRequest
      .post(createOrderUrl)
      .body(request)
      .response(SttpResponseUtils.unwrapResponse[F, OrderResponse])
      .readTimeout(orderClientConfiguration.timeout)
      .send(sttpBackend)
      .flatMap(_.body)
//      .flatTap(saveToDatabase)
//      .flatMap(response => saveToDatabase(response).map(_ => response))
//      .flatMap(response => saveToDatabase(response).as(response))
  }

  def saveToDatabase[F[_]: Sync](r: OrderResponse): F[Unit] = Ref.of[F, Int](0)
    .flatMap(ref => ref
      .update(_ + 1)
      .flatTap(_ => ref.get)
      .flatMap(_ => ref.update(_ - 1))
    )

  override def findOrder(orderId: UUID): F[Option[OrderResponse]] = {
    val listPetsUrl = uri"${orderClientConfiguration.baseUrl}/api/v1/order/$orderId"

    basicRequest
      .get(listPetsUrl)
      .response(SttpResponseUtils.unwrapResponse[F, Option[OrderResponse]])
      .readTimeout(orderClientConfiguration.timeout)
      .send(sttpBackend)
      .flatMap(_.body)
  }
}
