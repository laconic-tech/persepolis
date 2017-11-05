package co.laconic.akka.persistence.jdbc.snapshots

import akka.persistence.snapshot.SnapshotStoreSpec
import co.laconic.akka.persistence.support.Database
import com.typesafe.config.ConfigFactory

class JdbcSnapshotStoreSpec extends SnapshotStoreSpec(ConfigFactory.load()) {
  override def beforeAll(): Unit = {
    super.beforeAll()
    Database.initialise()
  }
}
