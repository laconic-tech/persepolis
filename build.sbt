name := "persepolis"
version := "0.1"

// common properties
lazy val rootDir = file(".")
lazy val commonSettings = Seq(
  organization := "co.uk.laconic",
  version := "0.1",
  scalaVersion := "2.11.11",
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

)

lazy val common = (project in file("support/common"))
  .settings(commonSettings)

// modules
lazy val persistence = (project in file("support/persistence"))
    .dependsOn(common)
    .settings(commonSettings)
    .settings(libraryDependencies ++= Seq(
      Dependencies.akkaPersistence,
      Dependencies.scalikeJdbc,
      Dependencies.scalikeJdbcConfig,
      // test dependencies
      Dependencies.scalatest,
      Dependencies.akkaTestKit,
      Dependencies.akkaPersistenceTck,
      "com.h2database" % "h2" % "1.4.196" % "test"
    ))

lazy val http = (project in file("support/http"))
    .dependsOn(common)
    .settings(commonSettings)
    .settings(libraryDependencies ++= Seq(
      Dependencies.akkaHttp,
      Dependencies.akka,
      Dependencies.akkaStreams,
      // testing
      Dependencies.scalatest,
      Dependencies.akkaTestKit
    ))

// services
lazy val metadataService = (project in file("services/metadata"))
    .settings(commonSettings)
    .dependsOn(common)
    .dependsOn(persistence)
    .dependsOn(http)

// root
lazy val root = project.in(rootDir)
    .settings(commonSettings)
    .aggregate(
      // support
      common,
      persistence,
      http,

      // services
      metadataService
    )
