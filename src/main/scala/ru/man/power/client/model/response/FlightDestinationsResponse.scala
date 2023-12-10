package ru.man.power.client.model.response

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import ru.man.power.client.model.{Data, Dictionaries, Issue, Meta}
import ru.man.power.client.model.{Data, Dictionaries, Issue, Meta}


case class FlightDestinationsResponse(
    data: Seq[Data],
    dictionaries: Dictionaries,
    meta: Meta,
    warnings: Option[Seq[Issue]],
)

object FlightDestinationsResponse {
  implicit val flightDestinationDecoder: Decoder[FlightDestinationsResponse] =
    deriveDecoder[FlightDestinationsResponse]
}
