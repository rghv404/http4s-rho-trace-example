package routes

import cats.effect.Async
import cats.{Monad, MonadError}
import natchez.Trace
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import org.typelevel.log4cats.Logger
import redis.RedisModel.BluepipeDate
import redis.{DecodeError, KeyNotFound, RedisRepository}

class NativeRoutes[F[_]: Async: Trace: Logger] extends Http4sDsl[F] {

  def greet[F[_]: Monad: Trace](input: String): F[String] =
    Trace[F].span("greet") {
      for {
        _ <- Trace[F].put("input" -> input)
      } yield s"Hello $input!\n"
    }

  def routes(redisRepo: RedisRepository[F]): HttpRoutes[F] =
    HttpRoutes.of[F] { case GET -> Root / "hello" / name =>
      Trace[F].span("hello native routes") {
        redisRepo
          .getBluepipeDate()
          .value
          .flatMap({
            case Left(DecodeError(msg))    => Logger[F].error(s"Decode Error for Redis reponse $msg") *> InternalServerError()
            case Left(KeyNotFound(msg))    => Logger[F].error(s"Key Not Found $msg") *> InternalServerError()
            case Right(resp: BluepipeDate) => Ok(s"Hello $name and we got response ${resp.effective_date} and ${resp.created_at}")
            case _                         => InternalServerError()
          })
      }

//      case GET -> Root / "fail" =>
//        ev.raiseError(new RuntimeException("💥 Boom!"))

    }

}

object NativeRoutes {
  def apply[F[_]: Async: Trace: Logger](redisRepository: RedisRepository[F]): HttpRoutes[F] = new NativeRoutes[F].routes(redisRepository)
}
