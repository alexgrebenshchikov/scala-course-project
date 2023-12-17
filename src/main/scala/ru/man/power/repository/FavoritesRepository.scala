package ru.man.power.repository

import ru.man.power.client.FlightDestinationsClient
import ru.man.power.repository.entities.FavoritesEntity

import java.sql.SQLException

trait FavoritesRepository[F[_]] {
  def addToFavorites(favoritesEntity: FavoritesEntity): F[Either[SQLException, Long]]
  def getFavorites(userLogin: String): F[Either[SQLException, List[FavoritesEntity]]]
  def deleteFavorites(userLogin: String): F[Either[SQLException, Long]]
}
