package com.leysoft.products.adapter.in.api

import cats.effect.{Effect, Timer}
import cats.syntax.all._
import dev.profunktor.tracer.Trace.Trace
import dev.profunktor.tracer.{Http4sTracerDsl, Trace, TracedHttpRoute, Tracer, TracerLog}
import org.http4s.server.Router
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.{HttpRoutes, Response}

final class TracedRoute[F[_]: Effect: Timer: Tracer] private (
  trace: TracedService[Trace[F, *]]
)(implicit L: TracerLog[Trace[F, *]])
    extends Http4sTracerDsl[F] {

  private val PathPrefix = "/trace"

  private def httpRoutes(
    errorHandler: PartialFunction[Throwable, F[Response[F]]]
  ): HttpRoutes[F] =
    TracedHttpRoute[F] {
      case GET -> Root using traceId =>
        trace.trace
          .run(traceId)
          .flatMap(Ok(_))
          .handleErrorWith(errorHandler)
      case GET -> Root / "websocket" using traceId =>
        import scala.concurrent.duration._
        val toClient: fs2.Stream[F, WebSocketFrame] = fs2.Stream
          .awakeEvery[F](1 seconds)
          .evalMap(i =>
            Trace(_ => Effect[F].delay(WebSocketFrame.Text(i.toString)))
              .run(traceId)
          )
        val fromClient: fs2.Pipe[F, WebSocketFrame, Unit] = _.evalMap {
          case WebSocketFrame.Text(str, _) =>
            L.info[TracedRoute[F]](str).run(traceId)
          case _ => L.info[TracedRoute[F]]("Error").run(traceId)
        }.covary[F]
        WebSocketBuilder[F].build(toClient, fromClient)
    }

  def routes(
    errorHandler: PartialFunction[Throwable, F[Response[F]]]
  ): HttpRoutes[F] =
    Router(PathPrefix -> httpRoutes(errorHandler))
}

object TracedRoute {

  def make[F[_]: Effect: Timer: Tracer](
    trace: TracedService[Trace[F, *]]
  )(implicit L: TracerLog[Trace[F, *]]): F[TracedRoute[F]] =
    Effect[F].delay(new TracedRoute[F](trace))
}

trait TracedService[F[_]] {

  def trace: F[Unit]
}

final class DefaultTracedService[F[_]: Effect] private (
  repository: TracedRepository[Trace[F, *]]
)(implicit
  val L: TracerLog[Trace[F, *]]
) extends TracedService[Trace[F, *]] {

  override def trace: Trace[F, Unit] =
    L.info[DefaultTracedService[F]]("Init") *>
      repository.get("ID") *> L.info[DefaultTracedService[F]]("End")
}

object DefaultTracedService {

  def make[F[_]: Effect](repository: TracedRepository[Trace[F, *]])(implicit
    L: TracerLog[Trace[F, *]]
  ): F[TracedService[Trace[F, *]]] =
    Effect[F].delay(new DefaultTracedService[F](repository))
}

trait TracedRepository[F[_]] {

  def get(id: String): F[String]
}

final class DefaultTracedRepository[F[_]: Effect] private (implicit
  val L: TracerLog[Trace[F, *]]
) extends TracedRepository[Trace[F, *]] {

  override def get(id: String): Trace[F, String] =
    L.info[DefaultTracedRepository[F]]("Init") *>
      Trace(_ => Effect[F].delay(id)) <* L.info[DefaultTracedRepository[F]](
      "End"
    )
}

object DefaultTracedRepository {

  def make[F[_]: Effect](implicit
    L: TracerLog[Trace[F, *]]
  ): F[TracedRepository[Trace[F, *]]] =
    Effect[F].delay(new DefaultTracedRepository[F])
}
