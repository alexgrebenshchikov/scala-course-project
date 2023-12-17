package ru.man.power.repository.entities

case class UserPasswordEntity(user_login: String, password_hash: String, salt: String)
