package ru.man.power.client.model.configuration

case class PostgresConfig(url: String, user: String, password: String, poolSize: Int)

case class HttpServer(port: Int)

case class FlightDestinationsAppConfig(database: PostgresConfig, http: HttpServer)


