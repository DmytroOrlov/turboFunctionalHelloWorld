import Dependencies._

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"

lazy val `turboFunctionalHelloWorld` = (project in file("."))
  .settings(
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full),
    scalacOptions ++= Seq(
      "-language:higherKinds",
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-unchecked",
      "-language:reflectiveCalls"
    ),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "1.0.0-RC17",
      "io.7mind.izumi" %% "distage-testkit" % "0.10.0-M2",
      scalaTest % Test,
    )
  )
