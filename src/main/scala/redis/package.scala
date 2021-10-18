import Config.RedisConfig
import cats.effect.Async
import cats.effect.Resource
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.RedisCommands
import dev.profunktor.redis4cats.connection.RedisClient
import dev.profunktor.redis4cats.data.RedisCodec
import dev.profunktor.redis4cats.effect.Log.Stdout.instance

package object redis {

  def redisClient[F[_]: Async](redisConf: RedisConfig): Resource[F, RedisCommands[F, String, String]] = {
    val stringCodec: RedisCodec[String, String] = RedisCodec.Utf8
    for {
      client <- RedisClient[F].from(redisConf.uri)
      redis  <- Redis[F].fromClient(client, stringCodec)
    } yield redis
  }
}
