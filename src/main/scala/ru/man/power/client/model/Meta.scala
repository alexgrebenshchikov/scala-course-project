package ru.man.power.client.model

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class Meta (currency: String, links: LinksSelf, defaults: Defaults)

object Meta {
  implicit val metaDecoder: Decoder[Meta] = deriveDecoder[Meta]
}
