package com.leysoft.products.adapter.in.api

import cats.effect.Effect
import cats.syntax.all._
import dev.profunktor.tracer.Trace.Trace
import dev.profunktor.tracer.{Http4sTracerDsl, TracedHttpRoute, Tracer, TracerLog}
import org.http4s.server.Router
import org.http4s.{HttpRoutes, Response}

final class TracedRoute[F[_]: Effect: Tracer] private (
  trace: TracedService[Trace[F, *]]
)(implicit L: TracerLog[Trace[F, *]])
    extends Http4sTracerDsl[F] {

  private val PathPrefix = "/trace"

  private def httpRoutes(
    errorHandler: PartialFunction[Throwable, F[Response[F]]]
  ): HttpRoutes[F] = TracedHttpRoute[F] {
    case GET -> Root using traceId =>
      trace.trace
        .run(traceId)
        .flatMap(Ok(_))
        .handleErrorWith(errorHandler)

  }

  def routes(
    errorHandler: PartialFunction[Throwable, F[Response[F]]]
  ): HttpRoutes[F] =
    Router(PathPrefix -> httpRoutes(errorHandler))
}

object TracedRoute {

  def make[F[_]: Effect: Tracer](
    trace: TracedService[Trace[F, *]]
  )(implicit L: TracerLog[Trace[F, *]]): F[TracedRoute[F]] =
    Effect[F].delay(new TracedRoute[F](trace))
}

trait TracedService[F[_]] {

  def trace: F[Unit]
}

final class DefaultTracedService[F[_]: Effect] private (
  implicit val L: TracerLog[Trace[F, *]]
) extends TracedService[Trace[F, *]] {

  override def trace: Trace[F, Unit] =
    L.info[DefaultTracedService[F]]("Init") *>
      L.info[DefaultTracedService[F]]("End")
}

object DefaultTracedService {

  def make[F[_]: Effect](
    implicit L: TracerLog[Trace[F, *]]
  ): F[TracedService[Trace[F, *]]] =
    Effect[F].delay(new DefaultTracedService[F])
}
