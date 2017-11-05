import sbt._

object Dependencies {


  val akkaVersion = "2.5.4"

  var akka = "com.typesafe.akka" %% "akka-actor"  % akkaVersion
  val akkaStreams = "com.typesafe.akka" %% "akka-stream" % akkaVersion
  val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
  val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % akkaVersion
  val akkaPersistenceTck = "com.typesafe.akka" %% "akka-persistence-tck" % akkaVersion % "test"
  val akkaHttpVersion = "10.0.10"
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion

  val scalikeJdbcVersion = "3.1.0"
  val scalikeJdbc = "org.scalikejdbc" %% "scalikejdbc" % scalikeJdbcVersion
  val scalikeJdbcConfig = "org.scalikejdbc" %% "scalikejdbc-config"  % scalikeJdbcVersion

  val scalatestVersion = "3.0.4"
  val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion % "test"
}
