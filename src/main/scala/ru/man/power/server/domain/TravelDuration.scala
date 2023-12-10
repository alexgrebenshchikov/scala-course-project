package ru.man.power.server.domain

import sttp.tapir.Schema
import tethys.{JsonReader, JsonWriter}
import tethys.derivation.semiauto.{jsonReader, jsonWriter}

final case class TravelDuration(lower: Int, upper: Int)

object TravelDuration {
  implicit val travelDurationReader: JsonReader[TravelDuration] = jsonReader

  implicit val travelDurationWriter: JsonWriter[TravelDuration] = jsonWriter

  implicit val travelDurationSchema: Schema[TravelDuration] = Schema.derived
    .description("Длительность путешествия.")
}
