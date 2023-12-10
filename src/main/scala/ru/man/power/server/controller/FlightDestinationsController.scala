package ru.man.power.server.controller

import ru.man.power.client.model.response.FlightDestinationsErrorResponse
import ru.man.power.server.common.controller.Controller
import ru.man.power.server.domain.request.RegistrationRequest
import ru.man.power.server.domain.{SearchParams, User}
import ru.man.power.server.domain.response.{
  ErrorResponse,
  FindFlightDestinationsResponse,
  SearchHistoryResponse,
}
import ru.man.power.server.service.{AuthService, FlightDestinationsService}
import sttp.model.Header
import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.server.{PartialServerEndpoint, ServerEndpoint}
import sttp.tapir._

import scala.concurrent.Future

class FlightDestinationsController[F[_]](
    flightDestinationsService: FlightDestinationsService[F],
    authService: AuthService[F],
) extends Controller[F] {
  private val secureEndpoint
      : PartialServerEndpoint[List[Header], User, Unit, ErrorResponse, Unit, Any, F] =
    endpoint
      .securityIn(headers)
      .errorOut(jsonBody[ErrorResponse])
      .serverSecurityLogic(authService.checkAccess)

  val findFlightDestinations: ServerEndpoint[Any, F] =
    secureEndpoint.post
      .summary("Поиск самых дешевых вариантов для путешествия на самолёте.")
      .in("api" / "v1" / "flight-destinations")
      .in(jsonBody[SearchParams])
      .out(jsonBody[FindFlightDestinationsResponse])
      //.errorOut(jsonBody[ErrorResponse])
      .serverLogic { (user: User) => (searchParams: SearchParams) =>
        flightDestinationsService.findFlightDestinations(user, searchParams)
      }

  val getSearchHistory: ServerEndpoint[Any, F] =
    secureEndpoint.get
      .summary("Получить свою историю поиска.")
      .in("api" / "v1" / "search-history")
      .out(jsonBody[SearchHistoryResponse])
      .serverLogicSuccess { (user: User) => _ =>
        flightDestinationsService.getSearchHistory(user)
      }

  val deleteSearchHistory: ServerEndpoint[Any, F] =
    secureEndpoint.delete
      .summary("Удалить свою историю поиска.")
      .in("api" / "v1" / "search-history")
      .serverLogicSuccess { (user: User) => _ =>
        flightDestinationsService.deleteSearchHistory(user)
      }

  val register: ServerEndpoint[Any, F] =
    endpoint.post
      .summary("Регистрация пользователя.")
      .in("api" / "v1" / "register")
      .in(jsonBody[RegistrationRequest])
      .errorOut(jsonBody[ErrorResponse])
      .serverLogic(authService.registerUser)

  val unregister: ServerEndpoint[Any, F] =
    secureEndpoint.delete
      .summary("Удалить свой профиль.")
      .in("api" / "v1" / "unregister")
      .serverLogic { (user: User) => _ =>
        authService.deleteUser(user)
      }

  override def endpoints: List[ServerEndpoint[Any, F]] =
    List(findFlightDestinations, getSearchHistory, deleteSearchHistory, register, unregister)
      .map(_.withTag("FlightDestinations"))
}

object FlightDestinationsController {
  def make[F[_]](
      orderService: FlightDestinationsService[F],
      authService: AuthService[F],
  ): FlightDestinationsController[F] =
    new FlightDestinationsController[F](orderService, authService)
}
