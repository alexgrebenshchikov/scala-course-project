package ru.man.power.repository.entities

import ru.man.power.server.domain.{SearchParams, TravelDuration}
import ru.man.power.server.domain.response.SearchHistoryResponse

case class SearchParamsEntity(
    user_login: String,
    origin: String,
    departure_date: String,
    one_way: Boolean,
    max_price: Int,
    travel_duration_lower: Int,
    travel_duration_upper: Int,
)

object SearchParamsEntity {
  def toResponse(searchParamsEntity: SearchParamsEntity): SearchParams = SearchParams(
    searchParamsEntity.origin,
    searchParamsEntity.departure_date,
    searchParamsEntity.one_way,
    searchParamsEntity.max_price,
    Some(
      TravelDuration(
        searchParamsEntity.travel_duration_lower,
        searchParamsEntity.travel_duration_upper,
      ),
    ),
  )
}
