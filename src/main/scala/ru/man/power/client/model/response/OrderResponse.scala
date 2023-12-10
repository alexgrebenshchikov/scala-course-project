package ru.man.power.client.model.response

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import ru.man.power.client.model.response

import java.time.Instant
import java.util.UUID

case class OrderResponse(id: UUID, petId: UUID, date: Instant)

object OrderResponse {
  implicit val orderResponseDecoder: Decoder[OrderResponse] = deriveDecoder[OrderResponse]
}
