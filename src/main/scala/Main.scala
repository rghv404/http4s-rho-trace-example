import Config.ProjectConfig
import cats.data.Kleisli
import cats.~>
import cats.effect.implicits._
import cats.implicits._
import shapeless.ops.nat
import natchez.noop.NoopSpan
import cats.effect.{Async, ExitCode, IO, IOApp, Resource, Sync}
import com.http4s.rho.swagger.ui.SwaggerUi
//import com.http4s.rho.swagger.ui.SwaggerUi
import dev.profunktor.redis4cats.RedisCommands
import io.circe.config.parser
import io.jaegertracing.Configuration.{ReporterConfiguration, SamplerConfiguration}
import org.http4s.rho.swagger.SwaggerSupport
import org.log4s.getLogger
import natchez._
import natchez.http4s.NatchezMiddleware
import natchez.http4s.implicits.toEntryPointOps
import natchez.jaeger.Jaeger
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits._
import org.http4s.rho.swagger.SwaggerMetadata
import org.http4s.rho.swagger.models.{Info, Tag}
import org.http4s.server.{Router, Server}
import org.typelevel.log4cats.{Logger, SelfAwareStructuredLogger}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import redis.RedisRepository
import routes.{MyRoutes, NativeRoutes}

object Main extends IOApp {

  private val logger = getLogger

  logger.info(s"Starting Swagger example on 8080")

  def entryPoint[F[_]: Sync]: Resource[F, EntryPoint[F]] =
    Jaeger.entryPoint[F](
      system = "rho-api-example",
      uriPrefix = Some(new java.net.URI("http://localhost:16686"))
    ) { c =>
      Sync[F].delay {
        c.withSampler(SamplerConfiguration.fromEnv)
          .withReporter(ReporterConfiguration.fromEnv)
          .getTracer
      }
    }

  def createServer[F[_]: Async]: Resource[F, Server] = {
    type K[A] = Kleisli[F, Span[F], A]

    implicit def slf4jLogger: SelfAwareStructuredLogger[Kleisli[F, Span[F], *]] = Slf4jLogger.getLogger[Kleisli[F, Span[F], *]]

    for {
      config                <- Resource.eval(parser.decodeF[F, ProjectConfig]())
      ep                    <- entryPoint[F]
      redisClient           <- redis.redisClient[K](config.redis).mapK(Kleisli.applyK(NoopSpan[F]))
      redisRepo              = RedisRepository[K](redisClient)
      metadata               = SwaggerMetadata(
                                 apiInfo = Info(title = "Example ro API", version = "0.1.0"),
                                 tags = List(Tag(name = "Bulk Data Request", description = Some("Submit POST requests containing the desired fields.")))
                               )
      rhoRoutes              = MyRoutes[Kleisli[F, Span[F], *]](redisRepo, metadata)
//      begin        = NativeRoutes(redisRepo)
//      middleware   = NatchezMiddleware.server(begin)
//      httpRoutes   = ep.liftT(middleware).orNotFound
//      httpRoutes   = ep.liftT(NatchezMiddleware.server(NativeRoutes(redisRepo))).orNotFound
      rhoRoutesFinal         = ep.liftT(NatchezMiddleware.server(rhoRoutes))
      routes                 = Router(
                                 "v1/rho" -> rhoRoutesFinal
//                                 "v1/native" -> httpRoutes
                               ).orNotFound
      server                <- BlazeServerBuilder[F]
                                 .bindHttp(config.server.port, config.server.host)
                                 .withHttpApp(routes)
                                 .resource
    } yield server
  }

  override def run(args: List[String]): IO[ExitCode] =
//    implicit def slf4jLogger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]
    createServer[IO].use(_ => IO.never).as(ExitCode.Success)
}
