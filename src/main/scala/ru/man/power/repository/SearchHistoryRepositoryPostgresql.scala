package ru.man.power.repository

import cats.effect.kernel.MonadCancelThrow
import cats.implicits.toFunctorOps
import doobie.Transactor
import io.getquill.SnakeCase
import io.getquill.doobie.DoobieContext
import doobie.implicits._
import ru.man.power.repository.entities.SearchParamsEntity

class SearchHistoryRepositoryPostgresql[F[_]: MonadCancelThrow](implicit tr: Transactor[F])
    extends SearchHistoryRepository[F] {

  private val ctx = new DoobieContext.Postgres(SnakeCase)

  import ctx._

  override def getUserSearchHistory(userLogin: String): F[List[SearchParamsEntity]] = run {
    quote {
      querySchema[SearchParamsEntity]("\"search_history\"").filter(_.user_login == lift(userLogin))
    }
  }.transact(tr)

  override def putUserSearchHistory(searchParams: SearchParamsEntity): F[Long] = run {
    quote {
      querySchema[SearchParamsEntity]("\"search_history\"").insertValue(lift(searchParams))
    }
  }.transact(tr)

  override def deleteUserSearchHistory(userLogin: String): F[Long] = run {
    quote {
      querySchema[SearchParamsEntity]("\"search_history\"")
        .filter(_.user_login == lift(userLogin))
        .delete
    }
  }.transact(tr)
}
