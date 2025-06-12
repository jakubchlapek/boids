ThisBuild / version := "0.0.1"

ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "boids"
  )

libraryDependencies += "org.scalafx" %% "scalafx" % "24.0.0-R35"
libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0"