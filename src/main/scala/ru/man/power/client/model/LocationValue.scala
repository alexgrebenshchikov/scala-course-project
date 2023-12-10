package ru.man.power.client.model

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class LocationValue(
    subType: String,
    detailedName: String,
)

object LocationValue {
  implicit val locationValueDecoder: Decoder[LocationValue] = deriveDecoder[LocationValue]
}


