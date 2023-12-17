package ru.man.power.client.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import sttp.tapir.Schema
import tethys.{JsonReader, JsonWriter}
import tethys.derivation.semiauto.{jsonReader, jsonWriter}

case class Price(
    total: String,
)

object Price {
  implicit val priceEncoder: Encoder[Price] = deriveEncoder[Price]

  implicit val priceDecoder: Decoder[Price] = deriveDecoder[Price]

  implicit val priceReader: JsonReader[Price] = jsonReader

  implicit val priceWriter: JsonWriter[Price] = jsonWriter

  implicit val priceSchema: Schema[Price] = Schema.derived
    .description("Цена билетов.")
}
