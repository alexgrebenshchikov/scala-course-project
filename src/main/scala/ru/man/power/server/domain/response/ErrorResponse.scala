package ru.man.power.server.domain.response

import sttp.tapir.Schema
import tethys.{JsonReader, JsonWriter}
import tethys.derivation.semiauto.{jsonReader, jsonWriter}

final case class ErrorResponse(errors: Seq[String])

object ErrorResponse {
  implicit val findFlightDestinationsErrorResponseReader: JsonReader[ErrorResponse] = jsonReader

  implicit val findFlightDestinationsErrorResponseWriter: JsonWriter[ErrorResponse] = jsonWriter

  implicit val findFlightDestinationsErrorResponseSchema: Schema[ErrorResponse] = Schema.derived
    .description("Описание ошибок запроса поиска направлений путешествий.")
}
