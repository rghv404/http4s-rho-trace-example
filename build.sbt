name := "http4s-rho-trace-example"

version := "0.1"

scalaVersion := "2.13.6"

val http4sVersion        = "0.23.5"
val rhoVersion           = "0.23.0-RC1"
val catsEffect           = "3.2.8"
val KindProjectorVersion = "0.13.1"

addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)

def library(group: String, artifact: String, version: String): ModuleID = group %% artifact % version

lazy val rho = library("org.http4s", _, rhoVersion)

lazy val http4sCore   = "org.http4s" %% "http4s-core"         % http4sVersion
lazy val http4sServer = "org.http4s" %% "http4s-server"       % http4sVersion
lazy val http4sDSL    = "org.http4s" %% "http4s-dsl"          % http4sVersion
lazy val http4sBlaze  = "org.http4s" %% "http4s-blaze-server" % http4sVersion
lazy val http4sCirce  = "org.http4s" %% "http4s-circe"        % http4sVersion

lazy val rhoCore      = rho("rho-core")
lazy val rhoSwagger   = rho("rho-swagger")
lazy val rhoSwaggerUI = rho("rho-swagger-ui")

lazy val http4sDeps  = Seq(http4sBlaze, http4sCore, http4sCirce, http4sDSL, http4sServer)
lazy val natchezDeps = Seq("org.tpolecat" %% "natchez-http4s" % "0.1.3", "org.tpolecat" %% "natchez-jaeger" % "0.1.5")
lazy val rhoDeps     = Seq(rhoCore, rhoSwagger, rhoSwaggerUI)

libraryDependencies ++= http4sDeps ++ rhoDeps ++ natchezDeps ++ Seq("org.tpolecat" %% "doobie-core" % "1.0.0-RC1")
