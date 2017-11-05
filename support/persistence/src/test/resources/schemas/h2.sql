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
    PRIMARY KEY(persistenceId, sequenceNr, timestamp)
)