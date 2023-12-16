package ru.man.power.server.domain

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import ru.man.power.repository.entities.SearchParamsEntity
import sttp.tapir.Schema
import tethys.{JsonReader, JsonWriter}
import tethys.derivation.semiauto.{jsonReader, jsonWriter}

case class SearchParams(
    origin: String,
    departureDate: String,
    oneWay: Boolean,
    maxPrice: Int,
    travelDuration: TravelDuration,
)

object SearchParams {
  implicit val searchParamsEncoder: Encoder[SearchParams] =
    deriveEncoder[SearchParams]

  implicit val searchParamsReader: JsonReader[SearchParams] = jsonReader

  implicit val searchParamsWriter: JsonWriter[SearchParams] = jsonWriter

  implicit val searchParamsSchema: Schema[SearchParams] = Schema.derived
    .description("Параметры поиска.")

  def toEntity(userLogin: String, searchParams: SearchParams): SearchParamsEntity =
    SearchParamsEntity(
      userLogin,
      searchParams.origin,
      searchParams.departureDate,
      searchParams.oneWay,
      searchParams.maxPrice,
      searchParams.travelDuration.lower,
      searchParams.travelDuration.upper
    )
}
