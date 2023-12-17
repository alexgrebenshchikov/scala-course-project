package ru.man.power.client.model

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import ru.man.power.repository.entities.FavoritesEntity
import ru.man.power.server.domain.FavoritesItem
import sttp.tapir.Schema
import tethys.{JsonReader, JsonWriter}
import tethys.derivation.semiauto.{jsonReader, jsonWriter}

case class Data(
    `type`: String,
    origin: String,
    destination: String,
    departureDate: String,
    returnDate: Option[String],
    price: Price,
    links: Links,
)

object Data {
  implicit val dataEncoder: Encoder[Data] =
    deriveEncoder[Data]

  implicit val dataDecoder: Decoder[Data] = deriveDecoder[Data]

  implicit val dataReader: JsonReader[Data] = jsonReader

  implicit val dataWriter: JsonWriter[Data] = jsonWriter

  implicit val dataSchema: Schema[Data] = Schema.derived
    .description("Данные о перелёте.")

  def toFavoritesEntity(userLogin: String, data: Data): FavoritesEntity =
    FavoritesEntity(
      userLogin,
      data.origin,
      data.destination,
      data.departureDate,
      data.returnDate.orNull,
      data.links.flightDates,
      data.links.flightOffers,
    )

  def toFavoritesItem(userLogin: String, data: Data): FavoritesItem =
    FavoritesEntity.toFavoritesItem(toFavoritesEntity(userLogin, data))

}
