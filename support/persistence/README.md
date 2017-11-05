# akka-persistence plugin, backed by scalikejdbc

Simple implementation of an akka persistence plugin that is lighter on dependencies - useful to be imported when working in restricted/process heavy environments. 

The current implementation relies on [scalikejdbc](http://scalikejdbc.org/) for interaction with the database store.

## Schema

Here is a sample schema based on an H2 database, you are free to add other fields as long as they are defaulted
```SQL
 DROP TABLE IF EXISTS journal;
 DROP TABLE IF EXISTS snapshots;
 
 CREATE TABLE IF NOT EXISTS journal (
     persistenceId VARCHAR(1024) NOT NULL,
     sequenceNr BIGINT NOT NULL,
     deleted CHAR DEFAULT 'N',
     event BLOB NOT NULL,
     PRIMARY KEY(persistenceId, sequenceNr)
 );
 
 CREATE TABLE IF NOT EXISTS snapshots (
     persistenceId VARCHAR(1024) NOT NULL,
     sequenceNr BIGINT NOT NULL,
     timestamp BIGINT NOT NULL,
     deleted CHAR DEFAULT 'N',
     snapshot BLOB NOT NULL,
     PRIMARY KEY(persistenceId, sequenceNr)
 )
```

## Usage 
In your `application.conf`, define the following
```hocon
akka {
  persistence {
    journal {
      plugin = "jdbc-journal"
      auto-start-journals = ["jdbc-journal"]
    }

    snapshot-store {
      plugin = "jdbc-snapshot"
      auto-start-snapshot-stores = ["jdbc-snapshot"]
    }
  }
}

db {
  default {
    // your database settings here ...
    // see http://scalikejdbc.org/documentation/configuration.html for more information
  }
}
```

You need to setup the database, this can be done by calling the lines below when your application starts.

```scala 
import scalikejdbc.config.DBs 
DBs.setupAll()
``` 

# Next steps

* Add a metadata field to journals so that events can be tagged