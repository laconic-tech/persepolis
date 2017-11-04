package co.laconic.akka.persistence.jdbc.snapshots

import java.io.InputStream

import akka.persistence.serialization.Snapshot
import akka.persistence._
import akka.serialization.Serialization

import scalikejdbc._

import co.laconic.common.io._

class JdbcSnapshotRepository(serialization: Serialization) {

  def load(persistenceId: String, fromSequenceNr: Option[Long], toSequenceNr: Option[Long], fromTimestamp: Option[Long], toTimestamp: Option[Long]): Option[SelectedSnapshot] =
    DB localTx { implicit session =>
      sql"""SELECT persistenceId, sequenceNr, timestamp, snapshot
              FROM snapshots
              WHERE persistenceId = $persistenceId
                AND sequenceNr   >= ISNULL($fromSequenceNr, sequenceNr)
                AND sequenceNr   <= ISNULL($toSequenceNr, sequenceNr)
                AND timestamp    >= ISNULL($fromTimestamp, timestamp)
                AND timestamp    <= ISNULL($toTimestamp, timestamp)
                AND deleted = 'N'
              ORDER BY sequenceNr DESC
        """
        .map { rs => rs
          SelectedSnapshot(
            SnapshotMetadata(rs.string("persistenceId"), rs.long("sequenceNr"), rs.long("timestamp")),
            deserialize(rs.blob("snapshot").getBinaryStream)
          )
        }
        .fetchSize(1)
        .headOption()
        .apply()
    }

  def save(metadata: SnapshotMetadata, snapshot: Any): Unit =
    DB localTx { implicit session =>
      sql"""INSERT INTO snapshots (persistenceId, sequenceNr, timestamp, deleted, snapshot)
            VALUES (
             ${metadata.persistenceId},
             ${metadata.sequenceNr},
             ${metadata.timestamp},
             'N',
             ${serialize(snapshot)}
            )
         """
        .update()
        .apply()
    }

  def delete(metadata: SnapshotMetadata): Unit =
    DB localTx { implicit session =>
      sql"""UPDATE snapshots
               SET deleted = 'Y'
             WHERE persistenceId = ${metadata.persistenceId}
               AND sequenceNr    = ${metadata.sequenceNr}
               AND timestamp     = ${metadata.timestamp}
        """
        .update()
        .apply()
    }

  def delete(persistenceId: String, fromSequenceNr: Option[Long], toSequenceNr: Option[Long], fromTimestamp: Option[Long], toTimestamp: Option[Long]): Unit =
    DB localTx { implicit session =>
      sql"""UPDATE snapshots
               SET deleted = 'Y'
             WHERE persistenceId = $persistenceId
               AND sequenceNr   >= ISNULL($fromSequenceNr, sequenceNr)
               AND sequenceNr   <= ISNULL($toSequenceNr, sequenceNr)
               AND timestamp    >= ISNULL($fromTimestamp, timestamp)
               AND timestamp    <= ISNULL($toTimestamp, timestamp)
        """
        .update()
        .apply()
    }

  private def serialize(snapshot: Any): Array[Byte] =
    serialization
      .serialize(Snapshot(snapshot))
      .get

  private def deserialize(is: InputStream): Any =
    serialization
      .deserialize(is.toByteArray, classOf[Snapshot])
      .map(_.data)
      .get
}
