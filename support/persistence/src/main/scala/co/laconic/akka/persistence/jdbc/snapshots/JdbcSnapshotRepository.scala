package co.laconic.akka.persistence.jdbc.snapshots

import java.io.InputStream

import akka.persistence.serialization.Snapshot
import akka.persistence._
import akka.serialization.Serialization
import co.laconic.akka.persistence.jdbc.serialization.Serializer
import scalikejdbc._


class JdbcSnapshotRepository(serialization: Serialization) {

  private val serializer = Serializer(serialization, classOf[Snapshot])

  def load(persistenceId: String, fromSequenceNr: Option[Long], toSequenceNr: Option[Long], fromTimestamp: Option[Long], toTimestamp: Option[Long]): Option[SelectedSnapshot] =
    DB localTx { implicit session =>
      sql"""SELECT persistenceId, sequenceNr, timestamp, snapshot
              FROM snapshots
              WHERE persistenceId = $persistenceId
                AND sequenceNr   >= COALESCE($fromSequenceNr, sequenceNr)
                AND sequenceNr   <= COALESCE($toSequenceNr,   sequenceNr)
                AND timestamp    >= COALESCE($fromTimestamp,   timestamp)
                AND timestamp    <= COALESCE($toTimestamp,     timestamp)
                AND deleted       = 'N'
           ORDER BY sequenceNr DESC
        """
        .map { rs => rs
          SelectedSnapshot(
            SnapshotMetadata(rs.string("persistenceId"), rs.long("sequenceNr"), rs.long("timestamp")),
            serializer.deserialize(rs.blob("snapshot").getBinaryStream).data
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
             ${serializer.serialize(Snapshot(snapshot))}
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
               AND sequenceNr   >= COALESCE($fromSequenceNr, sequenceNr)
               AND sequenceNr   <= COALESCE($toSequenceNr,   sequenceNr)
               AND timestamp    >= COALESCE($fromTimestamp,   timestamp)
               AND timestamp    <= COALESCE($toTimestamp,     timestamp)
        """
        .update()
        .apply()
    }
}
