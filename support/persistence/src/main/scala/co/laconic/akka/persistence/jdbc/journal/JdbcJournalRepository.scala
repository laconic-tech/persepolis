package co.laconic.akka.persistence.jdbc.journal

import akka.persistence._
import akka.serialization.Serialization
import co.laconic.akka.persistence.jdbc.serialization.Serializer
import scalikejdbc._

import scala.collection.immutable
import scala.language.postfixOps
import scala.util._

class JdbcJournalRepository(serialization: Serialization) {

  private val serializer = Serializer(serialization, classOf[PersistentRepr])

  def write(persistenceId: String, payload: immutable.Seq[PersistentRepr]): Try[Unit] = {
    Try {
      val parameters = payload.map(event => Seq(
          'persistenceId -> event.persistenceId,
          'sequenceNr -> event.sequenceNr,
          'deleted -> event.deleted,
          'event -> serializer.serialize(event)
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
      val fetchSize = if (max == Long.MaxValue || max > Int.MaxValue) Int.MaxValue else max.toInt
      sql"""SELECT event, deleted
              FROM journal
             WHERE persistenceId = $persistenceId
               AND sequenceNr   >= $fromSequenceNr
               AND sequenceNr   <= $toSequenceNr
          ORDER BY sequenceNr ASC
         """
        .map(rs =>
          serializer.deserialize(rs.blob("event").getBinaryStream).update(deleted = rs.string("deleted") == "Y")
        )
      .toTraversable()
      .apply()
      .take(fetchSize)
      .toSeq
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
}
