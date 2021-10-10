package com.http4s.rho.swagger.demo

import cats._
import cats.syntax.all._
import natchez.Trace
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object NativeRoutes {

  def greet[F[_]: Monad: Trace](input: String): F[String] =
    Trace[F].span("greet") {
      for {
        _ <- Trace[F].put("input" -> input)
      } yield s"Hello $input!\n"
    }

  def routes[F[_]: Trace](
                           implicit ev: MonadError[F, Throwable]
                         ): HttpRoutes[F] = {
    object dsl extends Http4sDsl[F]; import dsl._ // bleh
    HttpRoutes.of[F] {

      case GET -> Root / "hello" / name =>
        for {
          str <- greet[F](name)
          res <- Ok(str)
        } yield res

      case GET -> Root / "fail" =>
        ev.raiseError(new RuntimeException("💥 Boom!"))

    }
  }

}
