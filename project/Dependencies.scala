import sbt._

object Dependencies {
  val akkaVersion = "2.5.4"
  val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % akkaVersion
  val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"

  val scalikeJdbcVersion = "3.1.0"
  val scalikeJdbc = "org.scalikejdbc" %% "scalikejdbc" % scalikeJdbcVersion
  val scalikeJdbcConfig = "org.scalikejdbc" %% "scalikejdbc-config"  % scalikeJdbcVersion

  val scalatestVersion = "3.0.4"
  val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion % "test"
}
