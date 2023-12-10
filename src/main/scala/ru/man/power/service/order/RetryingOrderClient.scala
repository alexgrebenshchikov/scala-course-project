package ru.man.power.service.order

import cats.MonadThrow
import cats.effect.kernel.Async
import retry.{Sleep, retryingOnSomeErrors}
import ru.man.power.client.OrderClient
import ru.man.power.client.model.request.CreateOrderRequest
import ru.man.power.client.model.response.OrderResponse
import ru.man.power.commons.RetryUtils
import ru.man.power.client.model.response

import java.util.UUID

class RetryingOrderClient[F[_]: MonadThrow : Sleep](
    orderClient: OrderClient[F],
    retryUtils: RetryUtils[F],
) extends OrderClient[F] {
  override def createOrder(request: CreateOrderRequest): F[OrderResponse] =
    retryingOnSomeErrors[OrderResponse](
      isWorthRetrying = retryUtils.isTimeoutException,
      policy = retryUtils.policy,
      onError = retryUtils.onError,
    )(orderClient.createOrder(request))

  override def findOrder(orderId: UUID): F[Option[OrderResponse]] =
    retryingOnSomeErrors[Option[OrderResponse]](
      isWorthRetrying = retryUtils.isTimeoutException,
      policy = retryUtils.policy,
      onError = retryUtils.onError,
    )(orderClient.findOrder(orderId))
}
