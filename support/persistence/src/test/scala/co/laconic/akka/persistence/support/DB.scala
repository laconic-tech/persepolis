package co.laconic.akka.persistence.support

import scalikejdbc.DB
import scalikejdbc.config.DBs

import scala.io.Source

trait DB {
  // setup and create database
  DBs.setupAll()
  DB localTx { implicit session =>
    session.executeUpdate(Source.fromResource("schemas/h2.sql").mkString)
  }
}
