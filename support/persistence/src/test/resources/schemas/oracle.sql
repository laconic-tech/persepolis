BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE journal';
EXCEPTION
  WHEN OTHERS THEN NULL;
END;


CREATE TABLE journal (
    persistenceId VARCHAR(1024) NOT NULL,
    sequenceNr NUMBER(18) NOT NULL,
    deleted CHAR DEFAULT 'N',
    event BLOB NOT NULL,
    PRIMARY KEY(persistenceId, sequenceNr)
);

BEGIN
  EXECUTE IMMEDIATE 'DROP TABLE snapshots';
EXCEPTION
  WHEN OTHERS THEN NULL;
END;

CREATE TABLE snapshots (
    persistenceId VARCHAR(1024) NOT NULL,
    sequenceNr NUMBER(18) NOT NULL,
    timestamp NUMBER(18) NOT NULL,
    deleted CHAR DEFAULT 'N',
    snapshot BLOB NOT NULL,
    PRIMARY KEY(persistenceId, sequenceNr)
)