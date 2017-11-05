package co.laconic.services.metadata

import akka.actor.ActorSystem
import akka.http.scaladsl._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer

import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import scala.util.Try

case class HttpService(routes: RouteBuilder = RouteBuilder())
                      (implicit system:ActorSystem, mat: ActorMaterializer) {
  // metadata about this service
  lazy val id: String = system.name
  lazy val uptime: Long = (System.currentTimeMillis - system.startTime) / 1000
  lazy val config: Config = system.settings.config
  lazy val environment: String = config.getString("environment")
  lazy val version: String = ""
  lazy val builtOn: Long = 0L

  def getHealth: Route =
    path("health") {
      get {
        print("Ha!")

        complete(
          s"""
            | service: $id
            | environment: $environment
            | version: $version, built on $builtOn
            | uptime: $uptime seconds ago.
          """.stripMargin
        )
      }
    }

  def start(): Unit = {
    // default interface to bind to
    val interface = Try(config.getString("http.hostname")).getOrElse("0.0.0.0")
    val port = Try(config.getInt("http.port")).getOrElse(8081)

    // add default routes
    val r = routes.add(getHealth)
                  .build()

    implicit val ec: ExecutionContext = system.dispatcher

    Http(system)
      .bindAndHandle(getHealth, interface, port)
        .recover {
          case t: Throwable => print(t.getMessage); throw t
        }
      .onComplete(_ => system.terminate())

    System.in.read()
  }
}

case class RouteBuilder(routes: Seq[Route] = Seq.empty) {

  def add(route: Route): RouteBuilder =
    copy(routes = routes :+ route)

  def build(): Route = routes.reduce(_ ~ _)
}


object MetadataService extends App {

  implicit val system: ActorSystem = ActorSystem("Metadata")
  implicit val mat: ActorMaterializer = ActorMaterializer()

  // start listening
  HttpService()
    .start()
}