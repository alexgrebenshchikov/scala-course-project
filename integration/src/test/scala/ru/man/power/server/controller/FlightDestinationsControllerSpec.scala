package ru.man.power.server.controller

import cats.effect.testing.scalatest.{AsyncIOSpec, CatsResourceIO}
import cats.effect.{IO, Resource}
import com.dimafeng.testcontainers.PostgreSQLContainer
import com.typesafe.config.ConfigFactory
import doobie.Transactor
import org.scalatest.flatspec.FixtureAsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import org.testcontainers.containers.wait.strategy.Wait.defaultWaitStrategy
import org.testcontainers.utility.DockerImageName
import ru.man.power.client.FlightDestinationsClientImplSpecUtils
import ru.man.power.client.model.configuration.{FlightDestinationsClientConfiguration, PostgresConfig}
import ru.man.power.database.FlywayMigration
import ru.man.power.database.Transactor.makeTransactor
import ru.man.power.repository.{AccessTokenRepositoryPostgresql, FavoritesRepositoryPostgresql, PasswordsRepositoryPostgresql, SearchHistoryRepositoryPostgresql}
import ru.man.power.server.domain.request.RegistrationRequest
import ru.man.power.server.domain.response.FindFlightDestinationsResponse
import ru.man.power.server.service.{RepositoryAuthService, RepositoryFlightDestinationsService}
import ru.man.power.utils.TestData.{dataList, searchParams}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client3.circe._
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{UriContext, basicRequest}
import sttp.model.Header
import sttp.tapir.integ.cats.effect.CatsMonadError
import sttp.tapir.server.stub.TapirStubInterpreter

class FlightDestinationsControllerSpec
    extends FixtureAsyncFlatSpec
    with AsyncIOSpec
    with CatsResourceIO[Transactor[IO]]
    with Matchers
    with FlightDestinationsClientImplSpecUtils {

  private def createBackendStub(implicit xa: Transactor[IO]) = {
    val config = ConfigFactory.load()
    val flightDestinationsClientConfiguration: FlightDestinationsClientConfiguration =
      FlightDestinationsClientConfiguration.loadConfig(config)

    val tokenRepo = new AccessTokenRepositoryPostgresql[IO]()
    val searchHistoryRepo = new SearchHistoryRepositoryPostgresql[IO]()
    val passwordsRepo = new PasswordsRepositoryPostgresql[IO]()
    val favoritesRepo = new FavoritesRepositoryPostgresql[IO]()

    for {
      sttpBackend <- AsyncHttpClientCatsBackend[IO]()

      controller = FlightDestinationsController.make(
        new RepositoryFlightDestinationsService(
          client,
          tokenRepo,
          searchHistoryRepo,
          favoritesRepo,
        ),
        new RepositoryAuthService(passwordsRepo),
      )
      backendStub = TapirStubInterpreter(SttpBackendStub(new CatsMonadError[IO]()))
        .whenServerEndpointRunLogic(controller.register)
        .whenServerEndpointRunLogic(controller.getSearchHistory)
        .whenServerEndpointRunLogic(controller.findFlightDestinations)
        .backend()
    } yield backendStub
  }

  it should "return hello message" in { implicit t =>
    for {
      backendStub <- createBackendStub
      response = basicRequest
        .post(uri"http://test.com/api/v1/register")
        .body(RegistrationRequest("login", "password"))
        .send(backendStub)
      _ <- response.asserting(_.body shouldBe Right(""))

      response = basicRequest
        .post(uri"http://test.com/api/v1/register")
        .body(RegistrationRequest("login", "password"))
        .send(backendStub)
      _ <- response.asserting(
        _.body shouldBe Left("{\"errors\":[\"Unable to register provided user\"]}"),
      )
    } yield ()

  }

  it should "return flight destinations" in { implicit t =>
    for {
      backendStub <- createBackendStub
      // "api" / "v1" / "flight-destinations"
      response = basicRequest
        .post(uri"http://test.com/api/v1/flight-destinations")
        .headers(Header("login", "login"), Header("password", "password"))
        .body(searchParams)
        .send(backendStub)
      _ <- response.asserting(r =>
        parseAsJsonUnsafe(r.body.getOrElse(???)).as[FindFlightDestinationsResponse] shouldBe Right(
          FindFlightDestinationsResponse(dataList),
        ),
      )
    } yield ()
  }

  private def containerResource: Resource[IO, PostgreSQLContainer] =
    Resource.make(
      IO {
        val c = PostgreSQLContainer
          .Def(
            dockerImageName = DockerImageName.parse("postgres"),
          )
          .start()
        c.container.waitingFor(defaultWaitStrategy)
        c
      },
    )(c => IO(c.stop()))

  override val resource: Resource[IO, Transactor[IO]] =
    for {
      c <- containerResource
      conf = PostgresConfig(
        c.jdbcUrl,
        user = c.username,
        password = c.password,
        poolSize = 2,
      )
      _ <- Resource.eval(FlywayMigration.migrate[IO](conf))
      tx <- makeTransactor[IO](conf)
    } yield tx
}
