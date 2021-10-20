package redis

import cats.data.EitherT
import cats.effect.Async
import dev.profunktor.redis4cats.RedisCommands
import natchez.Trace
import redis.RedisModel._

import scala.util.Try
import java.time.LocalDate

class RedisRepository[F[_]: Async: Trace](redisConn: RedisCommands[F, String, String]) {

  def getBluepipeDate(): EitherT[F, JobError, BluepipeDate] =
    for {
      effectiveDate <- convertValue("bluepipe_date", "effective_date", x => Try(LocalDate.parse(x, yyyyMMdd)).toEither)
      createdAt     <- convertValue("bluepipe_date", "created_at", x => Try(x.toLong).toEither)
    } yield BluepipeDate(effectiveDate, createdAt)

  private def convertValue[A, B](
    key: String,
    field: String,
    convert: String => Either[A, B]
  ): EitherT[F, JobError, B] = {
    import cats.implicits._

    EitherT(
      for {
        maybeValue <- redisConn.hGet(key, field)
        x           = maybeValue match {
                        case Some(value) =>
                          convert(value) match {
                            case Right(convertedValue) => Right(convertedValue)
                            case Left(error)           =>
                              Left(
                                DecodeError(
                                  s"Unexpected Redis value $value for key $key and field $field: $error"
                                )
                              )
                          }
                        case None        => Left(KeyNotFound(s"Redis key $key, field $field"))
                      }
      } yield x
    )
  }
}

object RedisRepository {
  def apply[F[_]: Async: Trace](redisConn: RedisCommands[F, String, String]): RedisRepository[F] =
    new RedisRepository[F](redisConn)
}
