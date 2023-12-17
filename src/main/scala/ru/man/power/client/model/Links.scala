package ru.man.power.client.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import sttp.tapir.Schema
import tethys.{JsonReader, JsonWriter}
import tethys.derivation.semiauto.{jsonReader, jsonWriter}


case class Links (flightDates: String, flightOffers: String)

object Links {
  implicit val linksEncoder: Encoder[Links] = deriveEncoder[Links]

  implicit val linksDecoder: Decoder[Links] = deriveDecoder[Links]

  implicit val linksReader: JsonReader[Links] = jsonReader

  implicit val linksWriter: JsonWriter[Links] = jsonWriter

  implicit val linksSchema: Schema[Links] = Schema.derived
    .description("Ссылки на оформление билетов.")
}
