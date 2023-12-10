package ru.man.power.repository

import cats.effect.kernel.MonadCancelThrow
import doobie.Transactor
import io.getquill.SnakeCase
import io.getquill.doobie.DoobieContext
import ru.man.power.repository.entities.FavoritesEntity
import doobie.implicits._

import java.sql.SQLException

class FavoritesRepositoryPostgresql[F[_]: MonadCancelThrow](implicit tr: Transactor[F])
  extends FavoritesRepository[F] {

  private val ctx = new DoobieContext.Postgres(SnakeCase)

  import ctx._

  override def addToFavorites(favoritesEntity: FavoritesEntity): F[Either[SQLException, Long]] = run {
    quote {
      querySchema[FavoritesEntity]("\"favorites\"").insertValue(lift(favoritesEntity))
    }
  }.transact(tr).attemptSql

  override def getFavorites(userLogin: String): F[Either[SQLException, List[FavoritesEntity]]] = run {
    quote {
      querySchema[FavoritesEntity]("\"favorites\"").filter(_.user_login == lift(userLogin))
    }
  }.transact(tr).attemptSql

  override def deleteFavorites(userLogin: String): F[Either[SQLException, Long]] = run {
    quote {
      querySchema[FavoritesEntity]("\"favorites\"").filter(_.user_login == lift(userLogin)).delete
    }
  }.transact(tr).attemptSql
}
