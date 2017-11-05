package co.laconic.akka.persistence.jdbc.journal

import akka.persistence.CapabilityFlag
import akka.persistence.journal.JournalSpec
import co.laconic.akka.persistence.support.Database
import com.typesafe.config.ConfigFactory

class JdbcAsyncWriteJournalSpec extends JournalSpec(ConfigFactory.load()) {

  override def supportsRejectingNonSerializableObjects: CapabilityFlag = false

  override def beforeAll(): Unit = {
    super.beforeAll()
    Database.initialise()
  }
}
