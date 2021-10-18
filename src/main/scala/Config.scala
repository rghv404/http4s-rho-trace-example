import io.circe.generic.auto._
import io.circe.generic.extras._

object Config {
  implicit val kebabCase: Configuration = Configuration.default.withKebabCaseMemberNames.withDefaults

  @ConfiguredJsonCodec case class ProjectConfig(
    server: Server,
    redis: RedisConfig
  )

  case class Server(
    host: String,
    port: Int
  )

  case class RedisConfig(
    host: String,
    port: Int
  ) {
    val uri = s"redis://$host:$port"
  }
}


//  def entryPoint[F[_]: Sync]: Resource[F, EntryPoint[F]] =
//    DDTracer.entryPoint[F](
//      buildFunc = builder =>
//        Sync[F].delay(
//          builder
//            .withProperties(
//              new Properties() {
//                put("writer.type", "LoggingWriter")
//              }
//            )
//            .serviceName("natchez-sample")
//            .build()
//        ),
//      uriPrefix = Some(new java.net.URI("https://app.datadoghq.com")) // https://app.datadoghq.eu for Europe
//    )