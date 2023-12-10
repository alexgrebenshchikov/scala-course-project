package ru.man.power.client.model

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import sttp.tapir.Schema
import tethys.derivation.semiauto.{jsonReader, jsonWriter}
import tethys.{JsonReader, JsonWriter}

case class Issue (status: Int, code: Long, title: String, detail: Option[String], source: Option[IssueSource])

object Issue {
  implicit val issueDecoder: Decoder[Issue] = deriveDecoder[Issue]

  implicit val issueReader: JsonReader[Issue] = jsonReader

  implicit val issueWriter: JsonWriter[Issue] = jsonWriter

  implicit val issueSchema: Schema[Issue] = Schema.derived
    .description("Описание ошибки.")
}



