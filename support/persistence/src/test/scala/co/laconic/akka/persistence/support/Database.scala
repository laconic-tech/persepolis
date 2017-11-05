package co.laconic.akka.persistence.support

import scalikejdbc._
import scalikejdbc.config.DBs

import scala.io.Source

trait Database {
  // setup and create database
  DBs.setupAll()

  // print out generated queries
  GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
    enabled = true, singleLineMode = true, logLevel = 'DEBUG
  )

  DB localTx { implicit session =>
    session.executeUpdate(Source.fromResource("schemas/h2.sql").mkString)
  }
}
