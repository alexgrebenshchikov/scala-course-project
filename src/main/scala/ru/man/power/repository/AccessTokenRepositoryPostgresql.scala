package ru.man.power.repository
import cats.effect.kernel.MonadCancelThrow
import cats.implicits.toFunctorOps
import doobie.Transactor
import doobie.implicits._
import io.getquill.SnakeCase
import io.getquill.doobie.DoobieContext
import ru.man.power.client.model.response.AccessTokenResponse
import ru.man.power.repository.entities.AccessTokenEntity

class AccessTokenRepositoryPostgresql[F[_] : MonadCancelThrow](implicit tr: Transactor[F]) extends ExternalApiAccessTokenRepository[F] {
  private val ctx = new DoobieContext.Postgres(SnakeCase)

  import ctx._

  override def getAccessToken: F[Option[AccessTokenEntity]] = run {
    quote {
      querySchema[AccessTokenEntity]("\"access_token\"")
    }
  }.transact(tr).map(_.headOption)

  override def putAccessToken(accessToken: AccessTokenEntity): F[Long] = {
    val actions = for {
      _ <- run {
        quote {
          querySchema[AccessTokenEntity]("\"access_token\"").delete
        }
      }
      res <- run {
        quote {
          querySchema[AccessTokenEntity]("\"access_token\"").insertValue(lift(accessToken))
        }
      }
    } yield res
    actions.transact(tr)
  }

  /*override def putAccessToken(accessToken: AccessToken): F[Long] = run {
    quote {
      querySchema[AccessToken]("\"access_token\"").insertValue(lift(accessToken))
    }
  }.transact(tr)*/
}
