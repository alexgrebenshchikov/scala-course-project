package ru.man.power.repository

import ru.man.power.repository.entities.SearchParamsEntity

trait SearchHistoryRepository[F[_]] {
  def getUserSearchHistory(userLogin: String): F[List[SearchParamsEntity]]
  def putUserSearchHistory(searchParams: SearchParamsEntity): F[Long]
  def deleteUserSearchHistory(userLogin: String): F[Long]
}
