package ru.man.power

import cats.MonadThrow
import cats.data.OptionT
import cats.effect.std.Env
import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.{Host, IpLiteralSyntax, Port}
import com.typesafe.config.{Config, ConfigFactory}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import ru.man.power.client.{FlightDestinationsClient, HttpFlightDestinationsClient}
import ru.man.power.client.model.configuration.{FlightDestinationsAppConfig, FlightDestinationsClientConfiguration, OrderClientConfiguration}
import ru.man.power.server.controller.FlightDestinationsController
import ru.man.power.server.service.{RepositoryAuthService, RepositoryFlightDestinationsService}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import cats.MonadThrow
import cats.data.OptionT
import cats.effect.std.Env
import cats.effect.{ExitCode, IO, IOApp}
import doobie.Transactor
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import ru.man.power.database.Transactor.makeTransactor
import ru.man.power.repository.{AccessTokenRepositoryPostgresql, PasswordsRepositoryPostgresql, SearchHistoryRepositoryPostgresql}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import ru.man.power.client.model.response.AccessTokenResponse
import ru.man.power.database.FlywayMigration
import ru.man.power.repository.entities.{SearchParamsEntity, UserPasswordEntity}

import java.util.UUID

/*object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val config: Config = ConfigFactory.load()
    val flightDestinationsClientConfiguration: FlightDestinationsClientConfiguration =
      FlightDestinationsClientConfiguration.load(config)
    //AsyncHttpClientCatsBackend[IO]().flatMap(
    //  sttpBackend => new HttpFlightDestinationsClient(sttpBackend, flightDestinationsClientConfiguration).findFlightDestinations("sdsd", "dsds", "dsdsd")
    //)
    for {
      sttpBackend <- AsyncHttpClientCatsBackend[IO]()
      response <- new HttpFlightDestinationsClient(
        sttpBackend,
        flightDestinationsClientConfiguration,
      ).findFlightDestinations2("sdsd", "dsds", "Bearer k8G4cHfJxbMWCCHzEqwnxBBVEss0")
      //).renewAccessToken()
      _ <- IO.println("#############################")
      _ <- IO.println(response)
    } yield ExitCode.Success
  }
}*/

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {

    val conf = ConfigSource.default.loadOrThrow[FlightDestinationsAppConfig]
    makeTransactor[IO](conf.database).use { implicit xa: Transactor[IO] =>
      val tokenRepo = new AccessTokenRepositoryPostgresql[IO]
      val searchHistoryRepo = new SearchHistoryRepositoryPostgresql[IO]()
      val passwordsRepo = new PasswordsRepositoryPostgresql[IO]()

      for {
        _ <- FlywayMigration.migrate[IO](conf.database)

        /*r1 <- searchHistoryRepo.putUserSearchHistory(
          SearchParamsEntity("aaai", "sasa", "sdsd", one_way = true, 2, 3, 4),
        )
        //rfr <- searchHistoryRepository.getUserSearchHistory("aaa")
        _ <- IO.println(r1)

        r2 <- searchHistoryRepo.deleteUserSearchHistory("aaa1")
        //rfr <- searchHistoryRepository.getUserSearchHistory("aaa")
        _ <- IO.println(r2)*/
        //r3 <- passwordsRepo.addUser(UserPasswordEntity("waka", "kok", "plpl"))
        //_ <- IO.println(r3)

        endpoints <- IO.delay {
          List(
            FlightDestinationsController.make(
              new RepositoryFlightDestinationsService(tokenRepo, searchHistoryRepo),
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
