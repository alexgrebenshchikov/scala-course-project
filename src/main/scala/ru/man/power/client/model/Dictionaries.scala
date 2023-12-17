package ru.man.power.client.model

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class Dictionaries(currencies: Map[String, String], locations: Map[String, LocationValue])

object Dictionaries {
  implicit val dictionariesDecoder: Decoder[Dictionaries] = deriveDecoder[Dictionaries]
}
