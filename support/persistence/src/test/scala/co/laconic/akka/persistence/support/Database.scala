package co.laconic.akka.persistence.support

import co.laconic.common.io._
import scalikejdbc._
import scalikejdbc.config.DBs

trait Database {

  Database.initialise()
}

object Database {
  def initialise(): Unit = {
    // setup and create database
    DBs.setupAll()

    // print out generated queries
    GlobalSettings.loggingSQLAndTime = LoggingSQLAndTimeSettings(
      enabled = true, singleLineMode = true, logLevel = 'DEBUG
    )

    DB localTx { implicit session =>
      session.executeUpdate(Resource("schemas/h2.sql").mkString)
    }
  }
}