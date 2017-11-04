package co.laconic.akka.persistence.jdbc.snapshots

import java.time.Instant

import akka.actor.ActorSystem
import akka.persistence.{SelectedSnapshot, SnapshotMetadata}
import akka.serialization.SerializationExtension
import akka.testkit.TestKit
import co.laconic.akka.persistence.support.DB
import org.scalatest.{Matchers, WordSpecLike}

class JdbcSnapshotRepositorySpec extends TestKit(ActorSystem("JdbcSnapshotStoreSpec"))
  with WordSpecLike
  with Matchers
  with DB {

  val target = new JdbcSnapshotRepository(SerializationExtension(system))

  "A Snapshot Repository" should {
    "be able to write, read and delete snapshots on the persistent store" in {

      val snapshots = List(
        SelectedSnapshot(SnapshotMetadata("test", 1, Instant.now.getEpochSecond), "test"),
        SelectedSnapshot(SnapshotMetadata("test", 2, Instant.now.getEpochSecond),"test + 1"),
        SelectedSnapshot(SnapshotMetadata("test", 3, Instant.now.getEpochSecond), "test + 2")
      )

      // add some snapshots
      snapshots.foreach {
        case SelectedSnapshot(metadata, snapshot) => target.save(metadata, snapshot)
      }

      // we should be able to retrieve it
      target.load("test", None, None, None, None) should be(Some(snapshots.last))
      target.load("test", Some(1), Some(1), None, None) should be(Some(snapshots.head))
      target.load("test", Some(1), Some(2), None, None) should be(Some(snapshots(1)))

      // we should be able to delete snapshots
      target.delete(snapshots.head.metadata)
      target.load("test", Some(1), Some(1), None, None) should be(None)

      // delete everything
      target.delete("test", None, None, None, None)
      target.load("test", None, None, None, None) should be(None)

    }
  }
}
