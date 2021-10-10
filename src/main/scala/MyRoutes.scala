import cats.Monad
import cats.effect.Async
import natchez.Trace
import org.http4s.rho.RhoRoutes
import org.http4s.rho.swagger.{SwaggerSupport, SwaggerSyntax}

class MyRoutes[F[_]: Async: Trace](swaggerSyntax: SwaggerSyntax[F]) extends RhoRoutes[F] {

//  val swaggerSyntax = SwaggerSupport.apply[F]

  import swaggerSyntax._

  val hello = "hello" @@ GET / "hello"

  "A variant of the hello route that takes an Int param" **
  hello / pathVar[Int] |>> {
    i: Int =>
      Trace[F].span("greet is hello") {
        Ok(s"You returned $i")
      }
  }
}
