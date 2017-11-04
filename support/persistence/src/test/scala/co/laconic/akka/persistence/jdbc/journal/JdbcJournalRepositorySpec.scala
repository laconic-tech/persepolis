package co.laconic.akka.persistence.jdbc.journal

import akka.actor.ActorSystem
import akka.persistence.PersistentRepr
import akka.serialization.SerializationExtension
import akka.testkit.TestKit
import co.laconic.akka.persistence.support.DB
import org.scalatest.{Matchers, WordSpecLike}

import scala.collection.immutable
import scala.util.Success

class JdbcJournalRepositorySpec extends TestKit(ActorSystem("JdbcJournalSpec"))
  with WordSpecLike
  with Matchers
  with DB {

  var target = new JdbcJournalRepository(SerializationExtension(system))

  "Jdbc Journal" should {
    "be able to query the state for a given persistence id" in {
      val persistenceId = "test"
      // the input events
      val events = immutable.Seq(
        PersistentRepr("1", persistenceId = persistenceId, sequenceNr = 1),
        PersistentRepr("2", persistenceId = persistenceId, sequenceNr = 2),
        PersistentRepr("3", persistenceId = persistenceId, sequenceNr = 3)
      )

      // writing should be successful
      target.write(persistenceId, events) should be(a[Success[Unit]])
      // we can read the events
      target.read(persistenceId, 0, 100, 100) should have size 3
      // the max sequenceNr should be 3
      target.getHighestSequenceNr(persistenceId, 0) should be (3)
      // delete should mark messages as deleted
      target.delete(persistenceId, 1)
      target.read(persistenceId, 0, 1, 100).head.deleted should be (true)
    }
  }
}
