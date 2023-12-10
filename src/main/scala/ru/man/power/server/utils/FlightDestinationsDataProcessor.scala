package ru.man.power.server.utils

import ru.man.power.client.model.Data

object FlightDestinationsDataProcessor {
  def sortByPrice(flights: Seq[Data]): Seq[Data] =
    flights.sortWith { case (data1, data2) =>
      (for {
          price1 <- data1.price.total.toDoubleOption
          price2 <- data2.price.total.toDoubleOption
      } yield price1 < price2).getOrElse(true)
    }
}
