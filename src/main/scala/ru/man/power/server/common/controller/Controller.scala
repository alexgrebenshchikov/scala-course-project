package ru.man.power.server.common.controller

import sttp.tapir.server.ServerEndpoint

trait Controller[F[_]] {
  def endpoints: List[ServerEndpoint[Any, F]]
}

