import cats.data.Kleisli
import cats.effect.kernel.Resource
import cats.effect._
import com.http4s.rho.swagger.ui.SwaggerUi
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

  def createServer[F[+_]: Async]: Resource[F, Server] = {
    type K[A] = Kleisli[F, Span[F], A]
    for {
      ep                    <- entryPoint[F]
      metadata               = SwaggerMetadata(
                                 apiInfo = Info(title = "Example ro API", version = "0.1.0"),
                                 tags = List(Tag(name = "Bulk Data Request", description = Some("Submit POST requests containing the desired fields.")))
                               )
      swaggerUiRhoMiddleware = SwaggerUi[K].createRhoMiddleware(swaggerMetadata = metadata.copy(basePath = Some("/v1/rho")))
      rhoRoutes              = new MyRoutes[K](SwaggerSupport.apply[K]).toRoutes(swaggerUiRhoMiddleware)
//      httpRoutes             = ep.liftT(NatchezMiddleware.server(NativeRoutes.routes)).orNotFound
      rhoRoutesFinal         = ep.liftT(NatchezMiddleware.server(rhoRoutes))
      routes                 = Router(
                                 "v1/rho" ->  rhoRoutesFinal// NatchezMiddleware.server(rhoRoutesFinal),
//                                 "v1/native" -> ep.liftT(NatchezMiddleware.server(httpRoutes))
                               ).orNotFound
      server                <- BlazeServerBuilder[F]
                                 .bindLocal(8080)
                                 .withHttpApp(routes)
                                 .resource
    } yield server
  }

  override def run(args: List[String]): IO[ExitCode] =
    createServer[IO].use(_ => IO.never).as(ExitCode.Success)
}
