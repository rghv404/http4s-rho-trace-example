name := "http4s-rho-trace-example"

version := "0.1"

scalaVersion := "2.13.6"

addCompilerPlugin(("org.typelevel" % "kind-projector" % "0.13.2").cross(CrossVersion.full))

libraryDependencies ++= Dependencies.deps
