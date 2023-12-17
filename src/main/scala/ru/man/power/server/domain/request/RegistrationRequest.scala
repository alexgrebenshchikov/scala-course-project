package ru.man.power.server.domain.request

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import sttp.tapir.Schema
import tethys.derivation.semiauto.{jsonReader, jsonWriter}
import tethys.{JsonReader, JsonWriter}

final case class RegistrationRequest(login: String, password: String)

object RegistrationRequest {
  implicit val registrationRequestEncoder: Encoder[RegistrationRequest] =
    deriveEncoder[RegistrationRequest]

  implicit val registrationRequestReader: JsonReader[RegistrationRequest] = jsonReader

  implicit val registrationRequestWriter: JsonWriter[RegistrationRequest] = jsonWriter

  implicit val registrationRequestSchema: Schema[RegistrationRequest] = Schema.derived
    .description("Запрос регистрации")
}
