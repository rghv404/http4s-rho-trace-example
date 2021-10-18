package routes

import cats.effect.Async
import natchez.Trace
import org.http4s.rho.RhoRoutes
import org.http4s.rho.swagger.SwaggerSyntax
import org.typelevel.log4cats.{Logger, SelfAwareStructuredLogger}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import redis.RedisModel.BluepipeDate
import redis.{DecodeError, KeyNotFound, RedisRepository}

class MyRoutes[F[+_]: Async: Trace: Logger](swaggerSyntax: SwaggerSyntax[F], redisRepository: RedisRepository[F]) extends RhoRoutes[F] {

  import swaggerSyntax._
  import cats.implicits._

  val hello = "hello" @@ GET / "hello"

  "A variant of the hello route that takes an Int param" **
    hello / pathVar[Int] |>> { i: Int =>
      Ok(s"You returned $i. Hope it's tracing")
      Trace[F].span("hello rho routes") {
        redisRepository
          .getBluepipeDate()
          .value
          .flatMap({
            case Left(DecodeError(msg))    => Logger[F].error(s"Decode Error for Redis reponse $msg") *> InternalServerError()
            case Left(KeyNotFound(msg))    => Logger[F].error(s"Key Not Found $msg") *> InternalServerError()
            case Right(resp: BluepipeDate) => Ok(s"You returned $i and we got response ${resp.effective_date} and ${resp.created_at}")
            case _                         => InternalServerError()
          })
      }
    }
}
