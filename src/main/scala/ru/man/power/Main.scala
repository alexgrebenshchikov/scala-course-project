package ru.man.power

import cats.MonadThrow
import cats.data.OptionT
import cats.effect.std.Env
import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.{Host, Port}
import com.typesafe.config.ConfigFactory
import doobie.Transactor
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.typelevel.log4cats.slf4j.Slf4jFactory
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import ru.man.power.client.{HttpFlightDestinationsClient, RetryingFlightDestinationsClient}
import ru.man.power.client.model.configuration.{FlightDestinationsAppConfig, FlightDestinationsClientConfiguration}
import ru.man.power.commons.RetryUtilsImpl
import ru.man.power.commons.configuration.RetryConfiguration
import ru.man.power.database.FlywayMigration
import ru.man.power.database.Transactor.makeTransactor
import ru.man.power.repository.{AccessTokenRepositoryPostgresql, FavoritesRepositoryPostgresql, PasswordsRepositoryPostgresql, SearchHistoryRepositoryPostgresql}
import ru.man.power.server.controller.FlightDestinationsController
import ru.man.power.server.service.{RepositoryAuthService, RepositoryFlightDestinationsService}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val conf = ConfigSource.default.loadOrThrow[FlightDestinationsAppConfig]
    val config = ConfigFactory.load()
    val flightDestinationsClientConfiguration: FlightDestinationsClientConfiguration =
      FlightDestinationsClientConfiguration.loadConfig(config)

    makeTransactor[IO](conf.database).use { implicit xa: Transactor[IO] =>
      val tokenRepo = new AccessTokenRepositoryPostgresql[IO]
      val searchHistoryRepo = new SearchHistoryRepositoryPostgresql[IO]()
      val passwordsRepo = new PasswordsRepositoryPostgresql[IO]()
      val favoritesRepo = new FavoritesRepositoryPostgresql[IO]()

      for {
        _ <- FlywayMigration.migrate[IO](conf.database)

        sttpBackend <- AsyncHttpClientCatsBackend[IO]()
        retryConfiguration = RetryConfiguration.load(config)
        logger = Slf4jFactory.create[IO].getLogger
        retryUtils = new RetryUtilsImpl[IO](logger, retryConfiguration)
        client = new HttpFlightDestinationsClient(
          sttpBackend,
          flightDestinationsClientConfiguration,
        )
        flightDestinationsClient = new RetryingFlightDestinationsClient[IO](client, retryUtils)

        endpoints <- IO.delay {
          List(
            FlightDestinationsController.make(
              new RepositoryFlightDestinationsService(
                flightDestinationsClient,
                tokenRepo,
                searchHistoryRepo,
                favoritesRepo,
              ),
              new RepositoryAuthService(passwordsRepo),
            ),
          ).flatMap(_.endpoints)
        }
        swagger = SwaggerInterpreter().fromServerEndpoints(
          endpoints,
          "flight-destinations",
          "1.0.0",
        )
        routes = Http4sServerInterpreter[IO]().toRoutes(swagger ++ endpoints)
        port <- IO.pure(getTestPort)
        _ <- EmberServerBuilder
          .default[IO]
          .withHost(Host.fromString("localhost").get)
          .withPort(port)
          .withHttpApp(Router("/" -> routes).orNotFound)
          .build
          .use { server =>
            for {
              _ <- IO.println(
                s"Go to http://localhost:${server.address.getPort}/docs to open SwaggerUI. Press ENTER key to exit.",
              )
              _ <- IO.readLine
            } yield ()
          }
      } yield ExitCode.Success
    }
  }

  private def getPort[F[_]: Env: MonadThrow]: F[Port] =
    OptionT(Env[F].get("HTTP_PORT"))
      .toRight("HTTP_PORT not found")
      .subflatMap(ps =>
        ps.toIntOption.toRight(s"Expected int in HTTP_PORT env variable, but got $ps"),
      )
      .subflatMap(pi => Port.fromInt(pi).toRight(s"No such port $pi"))
      .leftMap(new IllegalArgumentException(_))
      .rethrowT

  private def getTestPort[F[_]]: Port =
    Port.fromInt(8085).getOrElse(???)
}
