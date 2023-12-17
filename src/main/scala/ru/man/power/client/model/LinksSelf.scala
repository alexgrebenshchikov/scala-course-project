package ru.man.power.client.model

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class LinksSelf(self: String)


object LinksSelf {
  implicit val linksSelfDecoder: Decoder[LinksSelf] = deriveDecoder[LinksSelf]
}