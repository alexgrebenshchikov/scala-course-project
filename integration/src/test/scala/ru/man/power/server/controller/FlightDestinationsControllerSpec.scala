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
import ru.man.power.client.model.Data
import ru.man.power.client.model.Data.toFavoritesItem
import ru.man.power.client.model.configuration.{FlightDestinationsClientConfiguration, PostgresConfig}
import ru.man.power.database.FlywayMigration
import ru.man.power.database.Transactor.makeTransactor
import ru.man.power.repository.{AccessTokenRepositoryPostgresql, FavoritesRepositoryPostgresql, PasswordsRepositoryPostgresql, SearchHistoryRepositoryPostgresql}
import ru.man.power.server.domain.FavoritesItem
import ru.man.power.server.domain.request.RegistrationRequest
import ru.man.power.server.domain.response.{FavoritesResponse, FindFlightDestinationsResponse, SearchHistoryResponse}
import ru.man.power.server.service.{RepositoryAuthService, RepositoryFlightDestinationsService}
import ru.man.power.utils.TestData.{dataList, searchParams, testLogin, testPassword}
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client3.circe._
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{Response, SttpBackend, UriContext, basicRequest}
import sttp.model.Header
import sttp.tapir.integ.cats.effect.CatsMonadError
import sttp.tapir.server.stub.TapirStubInterpreter

class FlightDestinationsControllerSpec
    extends FixtureAsyncFlatSpec
    with AsyncIOSpec
    with CatsResourceIO[Transactor[IO]]
    with Matchers
    with FlightDestinationsClientImplSpecUtils {

  private def createBackendStub(implicit xa: Transactor[IO]): IO[SttpBackend[IO, Any]] = {
    val tokenRepo = new AccessTokenRepositoryPostgresql[IO]()
    val searchHistoryRepo = new SearchHistoryRepositoryPostgresql[IO]()
    val passwordsRepo = new PasswordsRepositoryPostgresql[IO]()
    val favoritesRepo = new FavoritesRepositoryPostgresql[IO]()

    val controller = FlightDestinationsController.make(
      new RepositoryFlightDestinationsService(
        client,
        tokenRepo,
        searchHistoryRepo,
        favoritesRepo,
      ),
      new RepositoryAuthService(passwordsRepo),
    )
    val backendStub = TapirStubInterpreter(SttpBackendStub(new CatsMonadError[IO]()))
      .whenServerEndpointRunLogic(controller.register)
      .whenServerEndpointRunLogic(controller.unregister)
      .whenServerEndpointRunLogic(controller.getSearchHistory)
      .whenServerEndpointRunLogic(controller.deleteSearchHistory)
      .whenServerEndpointRunLogic(controller.findFlightDestinations)
      .whenServerEndpointRunLogic(controller.addToFavorites)
      .whenServerEndpointRunLogic(controller.getFavorites)
      .whenServerEndpointRunLogic(controller.deleteFavorites)
      .backend()
    IO.pure(backendStub)
  }

  private def registerTestUser(
      backendStub: SttpBackend[IO, Any],
  ): IO[Response[Either[String, String]]] =
    basicRequest
      .post(uri"http://test.com/api/v1/register")
      .body(RegistrationRequest(testLogin, testPassword))
      .send(backendStub)

  private def findFlightDestinations(
      backendStub: SttpBackend[IO, Any],
  ): IO[Response[Either[String, String]]] =
    basicRequest
      .post(uri"http://test.com/api/v1/flight-destinations")
      .headers(Header("login", testLogin), Header("password", testPassword))
      .body(searchParams)
      .send(backendStub)

  it should "unregister user" in { implicit t =>
    for {
      backendStub <- createBackendStub
      response = registerTestUser(backendStub)
      _ <- response

      response = basicRequest
        .delete(uri"http://test.com/api/v1/unregister")
        .headers(Header("login", testLogin), Header("password", testPassword))
        .send(backendStub)
      _ <- response.asserting(_.body shouldBe Right(""))
    } yield ()

  }

  it should "return flight destinations" in { implicit t =>
    for {
      backendStub <- createBackendStub
      response = registerTestUser(backendStub)
      _ <- response

      response = findFlightDestinations(backendStub)
      _ <- response.asserting(r =>
        r.body
          .map(b => parseAsJsonUnsafe(b).as[FindFlightDestinationsResponse])
          .flatMap(identity) shouldBe Right(
          FindFlightDestinationsResponse(dataList),
        ),
      )
    } yield ()
  }

  it should "return search history" in { implicit t =>
    for {
      backendStub <- createBackendStub
      response = registerTestUser(backendStub)
      _ <- response

      response = basicRequest
        .delete(uri"http://test.com/api/v1/search-history")
        .headers(Header("login", testLogin), Header("password", testPassword))
        .send(backendStub)
      _ <- response.asserting(_.body shouldBe Right(""))

      response = findFlightDestinations(backendStub)
      _ <- response.asserting(r =>
        r.body
          .map(b => parseAsJsonUnsafe(b).as[FindFlightDestinationsResponse])
          .flatMap(identity) shouldBe Right(
          FindFlightDestinationsResponse(dataList),
        ),
      )

      response = basicRequest
        .get(uri"http://test.com/api/v1/search-history")
        .headers(Header("login", testLogin), Header("password", testPassword))
        .send(backendStub)
      _ <- response.asserting(r =>
        r.body
          .map(b => parseAsJsonUnsafe(b).as[SearchHistoryResponse])
          .flatMap(identity) shouldBe Right(
          SearchHistoryResponse(Seq(searchParams)),
        ),
      )
    } yield ()
  }

  it should "return favorites" in { implicit t =>
    for {
      backendStub <- createBackendStub
      response = registerTestUser(backendStub)
      _ <- response

      response = findFlightDestinations(backendStub)
      _ <- response.asserting(r =>
        r.body
          .map(b => parseAsJsonUnsafe(b).as[FindFlightDestinationsResponse])
          .flatMap(identity) shouldBe Right(
          FindFlightDestinationsResponse(dataList),
        ),
      )

      response = basicRequest
        .post(uri"http://test.com/api/v1/favorites")
        .headers(Header("login", testLogin), Header("password", testPassword))
        .body(dataList.head)
        .send(backendStub)
      _ <- response.asserting(_.body shouldBe Right(""))

      response = basicRequest
        .get(uri"http://test.com/api/v1/favorites")
        .headers(Header("login", testLogin), Header("password", testPassword))
        .send(backendStub)
      _ <- response.asserting(r =>
        r.body
          .map(b => parseAsJsonUnsafe(b).as[FavoritesResponse])
          .flatMap(identity) shouldBe Right(
          FavoritesResponse(Seq(toFavoritesItem(testLogin, dataList.head))),
        ),
      )

      response = basicRequest
        .delete(uri"http://test.com/api/v1/favorites")
        .headers(Header("login", testLogin), Header("password", testPassword))
        .send(backendStub)
      _ <- response.asserting(_.body shouldBe Right(""))

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
