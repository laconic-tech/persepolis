package co.laconic.akka.persistence.jdbc.journal

import akka.actor.ActorLogging
import akka.persistence._
import akka.persistence.journal.AsyncWriteJournal
import akka.serialization.SerializationExtension

import scala.collection.immutable
import scala.concurrent.Future
import scala.util.Try

class JdbcAsyncWriteJournal extends AsyncWriteJournal with ActorLogging {

  // create the journal backend
  private val journal = new JdbcJournalRepository(SerializationExtension(context.system))

  override def asyncWriteMessages(messages: immutable.Seq[AtomicWrite]): Future[immutable.Seq[Try[Unit]]] = {
    Future.fromTry {
      Try {
        messages.map { aw => journal.write(aw.persistenceId, aw.payload) }
      }
    }
  }

  override def asyncDeleteMessagesTo(persistenceId: String, toSequenceNr: Long): Future[Unit] = {
    log.debug("Deleting messages for `{}` up to sequenceNr: {}", persistenceId, toSequenceNr)
    Future.fromTry {
      Try {
        journal.delete(persistenceId, toSequenceNr)
      }
    }
  }

  override def asyncReplayMessages(persistenceId: String, fromSequenceNr: Long, toSequenceNr: Long, max: Long)(recoveryCallback: PersistentRepr => Unit): Future[Unit] = {
    log.debug("Replaying messages for `{}` for sequenceNr between {} and {}", persistenceId, fromSequenceNr, toSequenceNr)
    Future.fromTry {
      Try {
        journal.read(persistenceId, fromSequenceNr, toSequenceNr, max).foreach { msg => recoveryCallback(msg) }
      }
    }
  }

  override def asyncReadHighestSequenceNr(persistenceId: String, fromSequenceNr: Long): Future[Long] = {
    log.debug("Retrieving highest sequence for {}, starting from {}", persistenceId, fromSequenceNr)
    Future.fromTry {
      Try {
        val max = journal.getHighestSequenceNr(persistenceId, fromSequenceNr)
        log.debug("Max sequence for `{}` is `{}`", persistenceId, max)
        max
      }
    }
  }
}
