package ru.man.power.repository

import cats.Monad
import cats.effect.kernel.MonadCancelThrow
import cats.implicits.toFunctorOps
import doobie.Transactor
import io.getquill.SnakeCase
import io.getquill.doobie.DoobieContext
import doobie.implicits._
import ru.man.power.repository.entities.UserPasswordEntity

import java.sql.SQLException

class PasswordsRepositoryPostgresql[F[_]: MonadCancelThrow](implicit tr: Transactor[F])
    extends PasswordsRepository[F] {

  private val ctx = new DoobieContext.Postgres(SnakeCase)

  import ctx._


  override def addUser(userPasswordEntity: UserPasswordEntity): F[Either[SQLException, Long]] = run {
    quote {
      querySchema[UserPasswordEntity]("\"passwords\"").insertValue(lift(userPasswordEntity))
    }
  }.transact(tr).attemptSql

  override def getUser(userLogin: String): F[Either[SQLException, Option[UserPasswordEntity]]] = run {
    quote {
      querySchema[UserPasswordEntity]("\"passwords\"").filter(_.user_login == lift(userLogin))
    }
  }.transact(tr).map(_.headOption).attemptSql

  override def deleteUser(userLogin: String): F[Either[SQLException, Long]] = run {
    quote {
      querySchema[UserPasswordEntity]("\"passwords\"").filter(_.user_login == lift(userLogin)).delete
    }
  }.transact(tr).attemptSql
}
