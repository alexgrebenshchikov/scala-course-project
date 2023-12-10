package ru.man.power.server.domain.response

import ru.man.power.server.domain.SearchParams
import sttp.tapir.Schema
import tethys.{JsonReader, JsonWriter}
import tethys.derivation.semiauto.{jsonReader, jsonWriter}

final case class SearchHistoryResponse(
    searchHistory: Seq[SearchParams],
)

object SearchHistoryResponse {
  implicit val searchHistoryResponseReader: JsonReader[SearchHistoryResponse] = jsonReader

  implicit val searchHistoryResponseWriter: JsonWriter[SearchHistoryResponse] = jsonWriter

  implicit val searchHistoryResponseSchema: Schema[SearchHistoryResponse] = Schema.derived
    .description("История поиска.")
}

