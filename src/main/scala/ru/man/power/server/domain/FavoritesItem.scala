package ru.man.power.server.domain

import ru.man.power.client.model.Links
import sttp.tapir.Schema
import tethys.derivation.semiauto.{jsonReader, jsonWriter}
import tethys.{JsonReader, JsonWriter}

final case class FavoritesItem(
    userLogin: String,
    origin: String,
    destination: String,
    departureDate: String,
    return_date: String,
    links: Links,
)

object FavoritesItem {
  implicit val favoritesItemReader: JsonReader[FavoritesItem] = jsonReader

  implicit val favoritesItemWriter: JsonWriter[FavoritesItem] = jsonWriter

  implicit val favoritesItemSchema: Schema[FavoritesItem] = Schema.derived
    .description("Направление для перелёта.")
}
