package ru.man.power.client.model.request

import io.circe.{Encoder, Json}
import io.circe.generic.semiauto.deriveEncoder

import java.util.UUID

final case class CreateOrderRequest(petId: UUID)

object CreateOrderRequest {
  implicit val createOrderRequestEncoder: Encoder[CreateOrderRequest] =
    deriveEncoder[CreateOrderRequest]

}
