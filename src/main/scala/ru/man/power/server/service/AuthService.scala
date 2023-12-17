package ru.man.power.server.service

import cats.data.EitherT
import cats.effect.IO
import ru.man.power.repository.PasswordsRepository
import ru.man.power.repository.entities.UserPasswordEntity
import ru.man.power.server.domain.User
import ru.man.power.server.domain.request.RegistrationRequest
import ru.man.power.server.domain.response.ErrorResponse
import sttp.model.Header

import java.security.MessageDigest
import scala.util.Random

trait AuthService[F[_]] {
  def registerUser(registrationRequest: RegistrationRequest): F[Either[ErrorResponse, Unit]]
  def checkAccess(headers: List[Header]): F[Either[ErrorResponse, User]]
  def deleteUser(user: User): F[Either[ErrorResponse, Unit]]
}

class RepositoryAuthService(private val passwordsRepository: PasswordsRepository[IO])
    extends AuthService[IO] {
  private val SALT_LEN = 16

  private def getPasswordHash(password: String, salt: String): String = MessageDigest
    .getInstance("SHA-256")
    .digest((password + salt).getBytes("UTF-8"))
    .map("%02x".format(_))
    .mkString

  override def checkAccess(headers: List[Header]): IO[Either[ErrorResponse, User]] =
    (for {
      login <- EitherT.fromEither[IO](
        headers.find(_.name == "login").toRight(ErrorResponse(Seq("No login provided."))),
      )
      password <- EitherT.fromEither[IO](
        headers.find(_.name == "password").toRight(ErrorResponse(Seq("No login provided."))),
      )
      res <- EitherT(
        passwordsRepository
          .getUser(login.value)
          .map(_.left.map(_ => ErrorResponse(Seq("Authorization failed"))).flatMap {
            case Some(userPasswordEntity) =>
              if (
                userPasswordEntity.password_hash == getPasswordHash(
                  password.value,
                  userPasswordEntity.salt,
                )
              ) Right(User(userPasswordEntity.user_login))
              else Left(ErrorResponse(Seq("Wrong password.")))
            case None => Left(ErrorResponse(Seq("Wrong login.")))
          }),
      )
    } yield res).value

  override def registerUser(
      registrationRequest: RegistrationRequest,
  ): IO[Either[ErrorResponse, Unit]] = {
    val salt = Random.alphanumeric.filter(_.isLetter).take(SALT_LEN).toString()
    val passwordHash = getPasswordHash(registrationRequest.password, salt)
    passwordsRepository
      .addUser(UserPasswordEntity(registrationRequest.login, passwordHash, salt))
      .map(_.left.map(_ => ErrorResponse(Seq("Unable to register provided user"))).map(_ => ()))
  }

  override def deleteUser(user: User): IO[Either[ErrorResponse, Unit]] =
    passwordsRepository
      .deleteUser(user.login)
      .map(_.left.map(_ => ErrorResponse(Seq("Unable to delete provided user"))).map(_ => ()))
}
