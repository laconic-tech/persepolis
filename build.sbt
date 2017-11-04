name := "persepolis"
version := "0.1"
scalaVersion := "2.11.11"

// common properties
lazy val rootDir = file(".")

lazy val common = project in file("support/common")

// modules
lazy val persistence = (project in file("support/persistence"))
    .dependsOn(common)
    .settings(libraryDependencies ++= Seq(
      // akka dependencies
      "com.typesafe.akka" %% "akka-persistence" % "2.5.4",
      // data access
      "org.scalikejdbc" %% "scalikejdbc" % "3.1.0",
      "org.scalikejdbc" %% "scalikejdbc-config"  % "3.1.0",

      // test dependencies
      "org.scalatest" %% "scalatest" % "3.0.4" % "test",
      "com.typesafe.akka" %% "akka-testkit" % "2.5.4" % "test",
      "com.h2database" % "h2" % "1.4.196" % "test"
    ))

// root project
lazy val root = project.in(rootDir).aggregate(
  common,
  persistence
)
