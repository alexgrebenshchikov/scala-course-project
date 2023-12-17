package ru.man.power.server.domain.response

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import ru.man.power.server.domain.FavoritesItem
import sttp.tapir.Schema
import tethys.derivation.semiauto.{jsonReader, jsonWriter}
import tethys.{JsonReader, JsonWriter}

final case class FavoritesResponse(
    favorites: Seq[FavoritesItem],
)

object FavoritesResponse {
  implicit val favoritesResponseDecoder: Decoder[FavoritesResponse] =
    deriveDecoder[FavoritesResponse]

  implicit val favoritesResponseReader: JsonReader[FavoritesResponse] = jsonReader

  implicit val favoritesResponseWriter: JsonWriter[FavoritesResponse] = jsonWriter

  implicit val favoritesResponseSchema: Schema[FavoritesResponse] = Schema.derived
    .description("Избранные направления для перелёта.")
}
