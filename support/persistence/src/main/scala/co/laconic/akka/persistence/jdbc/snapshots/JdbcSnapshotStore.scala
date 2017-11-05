package co.laconic.akka.persistence.jdbc.snapshots

import akka.persistence.snapshot.SnapshotStore
import akka.persistence._
import akka.serialization.SerializationExtension

import scala.concurrent.Future
import scala.util.Try

class JdbcSnapshotStore extends SnapshotStore {

  val serializer = SerializationExtension(context.system)
  val snapshots = new JdbcSnapshotRepository(serializer)

  override def loadAsync(persistenceId: String, criteria: SnapshotSelectionCriteria): Future[Option[SelectedSnapshot]] =
    Future fromTry {
      Try {
        snapshots.load(
          persistenceId,
          toOption(criteria.minSequenceNr),
          toOption(criteria.maxSequenceNr),
          toOption(criteria.minTimestamp),
          toOption(criteria.maxTimestamp)
        )
      }
    }

  override def saveAsync(metadata: SnapshotMetadata, snapshot: Any): Future[Unit] =
    Future fromTry {
      Try {
        snapshots.save(metadata, snapshot)
      }
    }

  override def deleteAsync(metadata: SnapshotMetadata): Future[Unit] =
    Future fromTry {
      Try {
        snapshots.delete(metadata)
      }
    }

  override def deleteAsync(persistenceId: String, criteria: SnapshotSelectionCriteria): Future[Unit] =
    Future fromTry {
      Try {
        snapshots.delete(
          persistenceId,
          toOption(criteria.minSequenceNr),
          toOption(criteria.maxSequenceNr),
          toOption(criteria.minTimestamp),
          toOption(criteria.maxTimestamp)
        )
      }
    }

  def toOption(value: Long): Option[Long] = if (value == Long.MaxValue) None else Some(value)
}
