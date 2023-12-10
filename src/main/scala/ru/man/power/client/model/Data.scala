package ru.man.power.client.model

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sttp.tapir.Schema
import tethys.{JsonReader, JsonWriter}
import tethys.derivation.semiauto.{jsonReader, jsonWriter}

case class Data(
    `type`: String,
    origin: String,
    destination: String,
    departureDate: String,
    returnDate: String,
    price: Price,
    links: Links,
)

object Data {
  implicit val dataDecoder: Decoder[Data] = deriveDecoder[Data]

  implicit val dataReader: JsonReader[Data] = jsonReader

  implicit val dataWriter: JsonWriter[Data] = jsonWriter

  implicit val dataSchema: Schema[Data] = Schema.derived
    .description("Данные о перелёте.")
}
