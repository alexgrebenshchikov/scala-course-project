package ru.man.power.client.model

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class Defaults(
    departureDate: Option[String],
    oneWay: Option[Boolean],
    duration: Option[String],
    nonStop: Option[Boolean],
    viewBy: Option[String],
)

object Defaults {
  implicit val defaultsDecoder: Decoder[Defaults] = deriveDecoder[Defaults]
}
