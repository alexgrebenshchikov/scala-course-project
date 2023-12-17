package ru.man.power.client.model

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import ru.man.power.client.model.response.FlightDestinationsErrorResponse
import sttp.tapir.Schema
import tethys.derivation.semiauto.{jsonReader, jsonWriter}
import tethys.{JsonReader, JsonWriter}

case class IssueSource(pointer: Option[String], parameter: Option[String], example: Option[String])

object IssueSource {
  implicit val issueSourceDecoder: Decoder[IssueSource] = deriveDecoder[IssueSource]

  implicit val issueSourceReader: JsonReader[IssueSource] = jsonReader

  implicit val issueSourceWriter: JsonWriter[IssueSource] = jsonWriter

  implicit val issueSourceSchema: Schema[IssueSource] = Schema.derived
    .description("Описание источника ошибки.")
}
