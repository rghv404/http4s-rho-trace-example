import sbt._

object Dependencies {

  val circe                = "0.14.1"
  val CirceConfigVersion   = "0.8.0"
  val http4sVersion        = "0.23.5"
  val rhoVersion           = "0.23.0-RC1"
  val catsEffect           = "3.2.8"
  val KindProjectorVersion = "0.13.1"

  def library(group: String, artifact: String, version: String): ModuleID = group %% artifact % version

  lazy val circeCats  = library("io.circe", _, "0.14.1")
  lazy val redis4Cats = library("dev.profunktor", _, _)
  lazy val rho        = library("org.http4s", _, rhoVersion)
  lazy val typelevel  = library("org.typelevel", _, _)

  lazy val circeConfig        = "io.circe" %% "circe-config" % CirceConfigVersion
  lazy val circeCore          = circeCats("circe-core")
  lazy val circeGeneric       = circeCats("circe-generic")
  lazy val circeGenericExtras = circeCats("circe-generic-extras")
  lazy val circeLiteral       = circeCats("circe-literal")
  lazy val circeParser        = circeCats("circe-parser")

  lazy val http4sCore   = "org.http4s" %% "http4s-core"         % http4sVersion
  lazy val http4sServer = "org.http4s" %% "http4s-server"       % http4sVersion
  lazy val http4sDSL    = "org.http4s" %% "http4s-dsl"          % http4sVersion
  lazy val http4sBlaze  = "org.http4s" %% "http4s-blaze-server" % http4sVersion
  lazy val http4sCirce  = "org.http4s" %% "http4s-circe"        % http4sVersion

  lazy val redis4CatsEffects = redis4Cats("redis4cats-effects", "1.0.0")
  lazy val redis4CatsLogger  = redis4Cats("redis4cats-log4cats", "1.0.0")

  lazy val rhoCore      = rho("rho-core")
  lazy val rhoSwagger   = rho("rho-swagger")
  lazy val rhoSwaggerUI = rho("rho-swagger-ui")

  lazy val log4catsCore = typelevel("log4cats-core", "2.1.1")
  lazy val log4Cats     = typelevel("log4cats-slf4j", "2.1.1")

  lazy val circeExtraDeps  = Seq(circeGenericExtras, circeConfig)
  lazy val circeCommonDeps = Seq(circeCore, circeGeneric, circeLiteral, circeParser)
  lazy val http4sDeps      = Seq(http4sBlaze, http4sCore, http4sCirce, http4sDSL, http4sServer)
  lazy val natchezDeps     = Seq(
    "org.tpolecat" %% "natchez-http4s" % "0.1.3",
    "org.tpolecat" %% "natchez-noop" % "0.1.5",
    "org.tpolecat" %% "natchez-jaeger" % "0.1.5",
//    "org.tpolecat" %% "natchez-datadog" % "0.1.5"
  )
  lazy val rhoDeps         = Seq(rhoCore, rhoSwagger, rhoSwaggerUI)
  lazy val logDeps         = Seq(log4catsCore, log4Cats)
  lazy val redis4CatsDeps  = Seq(redis4CatsEffects, redis4CatsLogger)

  lazy val deps = http4sDeps ++ rhoDeps ++ natchezDeps ++ logDeps ++
    Seq("org.tpolecat" %% "doobie-core" % "1.0.0-RC1") ++
    redis4CatsDeps ++ circeCommonDeps ++ circeExtraDeps

}
