package ru.man.power.client.model

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class ErrorDict (code: Long)


object ErrorDict {
  implicit val errorDictDecoder: Decoder[ErrorDict] = deriveDecoder[ErrorDict]
}


