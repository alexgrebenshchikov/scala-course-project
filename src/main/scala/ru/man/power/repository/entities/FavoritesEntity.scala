package ru.man.power.repository.entities

import ru.man.power.client.model.Links
import ru.man.power.server.domain.FavoritesItem

case class FavoritesEntity(
    user_login: String,
    origin: String,
    destination: String,
    departure_date: String,
    return_date: String,
    flight_dates: String,
    flight_offers: String,
)

object FavoritesEntity {
  def toFavoritesItem(favoritesEntity: FavoritesEntity): FavoritesItem = FavoritesItem(
    favoritesEntity.user_login,
    favoritesEntity.origin,
    favoritesEntity.destination,
    favoritesEntity.departure_date,
    favoritesEntity.return_date,
    Links(favoritesEntity.flight_dates, favoritesEntity.flight_offers),
  )
}
