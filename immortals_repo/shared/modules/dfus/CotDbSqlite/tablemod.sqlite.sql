DROP TABLE geometry_columns;
DROP TABLE geometry_columns_auth;
DROP TABLE spatial_ref_sys;
DROP TABLE spatialite_history;
DROP TABLE views_geometry_columns;
DROP TABLE virts_geometry_columns;
DROP VIEW geom_cols_ref_sys;

BEGIN TRANSACTION;
CREATE TABLE CotEvent8ec8
(
  id INTEGER PRIMARY KEY,
  receiveTime DATETIME DEFAULT CURRENT_TIMESTAMP,
  uid TEXT NOT NULL,
  type TEXT NOT NULL,
  version TEXT,
  hae REAL NOT NULL,
  ce REAL NOT NULL,
  le REAL NOT NULL,
  time DATETIME NOT NULL,
  start DATETIME NOT NULL,
  stale DATETIME NOT NULL,
  how TEXT NOT NULL,
  detail TEXT,
  opex TEXT,
  qos TEXT,
  access TEXT,
  editable INTEGER,
  pointLat REAL,
  pointLon REAL,
  contactCallsign TEXT,
  contactEndpoint TEXT,
  imageHeight INTEGER,
  imageWidth INTEGER,
  imageMime TEXT,
  imageSize INT,
  imageData BLOB
);
INSERT INTO CotEvent8ec8(id, receiveTime, uid, type, version, hae, ce, le, time, start, stale, how, detail, opex, qos, access, editable) SELECT id, receiveTime, uid, type, version, hae, ce, le, time, start, stale, how, detail, opex, qos, access, editable FROM CotEvent;
DROP TABLE CotEvent;
ALTER TABLE CotEvent8ec8 RENAME TO CotEvent;
COMMIT;
