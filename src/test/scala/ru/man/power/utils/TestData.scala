package ru.man.power.utils

import ru.man.power.client.model.{Data, Links, Price}
import ru.man.power.server.domain.{SearchParams, TravelDuration}

import java.time.Instant
import java.util.UUID

object TestData {
  val searchParams: SearchParams = SearchParams("PAR", "2024-01-23", oneWay = true, 200, TravelDuration(1, 10))

  val dataList: List[Data] = List(
    Data(
      "flight-destination",
      "PAR",
      "CAS",
      "2022-09-06",
      "2022-09-11",
      Price("161.90"),
      Links("a.com", "b.com"),
    ),
    Data(
      "flight-destination",
      "PAR",
      "AYT",
      "2022-10-16",
      "2022-10-31",
      Price("181.50"),
      Links("a.com", "b.com"),
    ),
  )

  val flightDestinationsResponseString: String =
    s"""
       |{
       |    "data": [
       |        {
       |            "type": "${dataList.head.`type`}",
       |            "origin": "${dataList.head.origin}",
       |            "destination": "${dataList.head.destination}",
       |            "departureDate": "${dataList.head.departureDate}",
       |            "returnDate": "${dataList.head.returnDate}",
       |            "price": {
       |                "total": "${dataList.head.price.total}"
       |            },
       |            "links": {
       |              "flightDates": "${dataList.head.links.flightDates}",
       |              "flightOffers": "${dataList.head.links.flightOffers}"
       |            }
       |        },
       |        {
       |            "type": "${dataList.last.`type`}",
       |            "origin": "${dataList.last.origin}",
       |            "destination": "${dataList.last.destination}",
       |            "departureDate": "${dataList.last.departureDate}",
       |            "returnDate": "${dataList.last.returnDate}",
       |            "price": {
       |                "total": "${dataList.last.price.total}"
       |            },
       |            "links": {
       |              "flightDates": "${dataList.last.links.flightDates}",
       |              "flightOffers": "${dataList.last.links.flightOffers}"
       |            }
       |        }
       |    ]
       |}
       |""".stripMargin

  val testLogin: String = "testUser"
  val testPassword: String = "platypus"
}
