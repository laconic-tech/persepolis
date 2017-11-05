package co.laconic.akka.persistence.jdbc.journal

import java.io.InputStream

import akka.persistence.PersistentRepr
import akka.serialization.Serialization

import scala.collection.immutable
import scala.language.postfixOps
import scala.util._
import scalikejdbc._

import co.laconic.common.io._

class JdbcJournalRepository(serializer: Serialization) {

  def write(persistenceId: String, payload: immutable.Seq[PersistentRepr]): Try[Unit] = {
    Try {
      val parameters = payload.map(event => Seq(
          'persistenceId -> event.persistenceId,
          'sequenceNr -> event.sequenceNr,
          'deleted -> event.deleted,
          'event -> serialize(event)
        )
      )

      DB localTx { implicit session =>
        // execute the insert
        sql"""INSERT INTO journal (persistenceId, sequenceNr, deleted, event)
              VALUES ({persistenceId}, {sequenceNr}, {deleted}, {event})"""
          .batchByName(parameters : _*)
          .apply()
      }
    }
  }

  def read(persistenceId: String, fromSequenceNr: Long, toSequenceNr: Long, max: Long): Seq[PersistentRepr] = {
    DB localTx { implicit session =>
      sql"""SELECT event, deleted
              FROM journal
             WHERE persistenceId = $persistenceId
               AND sequenceNr   >= $fromSequenceNr
               AND sequenceNr   <= $toSequenceNr
          ORDER BY sequenceNr ASC
         """
        .map(rs => deserialize(rs.blob("event").getBinaryStream).update(deleted = rs.string("deleted") == "Y"))
        .fetchSize(Integer.MAX_VALUE)
        .toList()
        .apply()
    }
  }

  def getHighestSequenceNr(persistenceId: String, fromSequenceNr: Long): Long = {
    DB localTx { implicit session =>
      sql"""SELECT ISNULL(MAX(sequenceNr), 0) as sequenceNr
              FROM journal
             WHERE persistenceId = $persistenceId
         """
        .map(_.long("sequenceNr"))
        .headOption()
        .apply()
        .getOrElse(0) // if no results found then the maximum is 0
    }
  }

  def delete(persistenceId: String, toSequenceNr: Long): Unit = {
    DB localTx { implicit session =>
      sql"""UPDATE journal
               SET deleted       = 'Y'
             WHERE persistenceId = $persistenceId
               AND sequenceNr   <= $toSequenceNr
         """
        .update()
        .apply()
    }
  }

  private def serialize(persistentRepr: PersistentRepr): Array[Byte] =
    serializer.serialize(persistentRepr).get

  private def deserialize(inputStream: InputStream): PersistentRepr =
    serializer
      .deserialize[PersistentRepr](inputStream.toByteArray, classOf[PersistentRepr])
      .get
}
