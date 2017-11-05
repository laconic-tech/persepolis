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
      "com.h2database" % "h2" % "1.4.196" % "test"
    ))

// root project
lazy val root = project.in(rootDir)
    .settings(commonSettings)
    .aggregate(
      common,
      persistence
    )
