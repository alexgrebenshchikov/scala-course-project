package ru.man.power.repository

import ru.man.power.repository.entities.UserPasswordEntity

import java.sql.SQLException

trait PasswordsRepository[F[_]] {
  def addUser(userPasswordEntity: UserPasswordEntity): F[Either[SQLException, Long]]

  def getUser(userLogin: String): F[Either[SQLException, Option[UserPasswordEntity]]]

  def deleteUser(userLogin: String): F[Either[SQLException, Long]]
}
