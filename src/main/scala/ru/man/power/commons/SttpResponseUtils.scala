package ru.man.power.commons

import cats.{Applicative, ApplicativeThrow, Functor, Monad}
import io.circe.Decoder
import sttp.client3.{ConditionalResponseAs, DeserializationException, ResponseAs, ResponseException}
import sttp.client3.circe.{asJsonAlways, asJsonEither}
import sttp.client3.impl.cats.implicits.{asyncMonadError, monadError}
import sttp.monad.syntax.MonadErrorOps

object SttpResponseUtils {
  def unwrapResponse[F[_]: ApplicativeThrow, T: Decoder]: ResponseAs[F[T], Any] =
    asJsonAlways[T].map(ApplicativeThrow[F].fromEither(_))

  def unwrapResponseOrError[F[_]: Applicative, E: Decoder, T: Decoder]
      : ResponseAs[F[Either[ResponseException[E, io.circe.Error], T]], Any] =
    asJsonEither[E, T].map(x => Applicative[F].pure(x))
}
