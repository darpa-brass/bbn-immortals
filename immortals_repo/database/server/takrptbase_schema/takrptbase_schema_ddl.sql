SET search_path TO :default_schema; --Note default_schema is set in setup_takrptbase_schema.sh when psql is invoked.

CREATE TABLE source
(
  source_id varchar(16) NOT NULL,
  name varchar(16) NOT NULL,
  channel varchar(16) NOT NULL,
  CONSTRAINT source_pkey PRIMARY KEY (source_id)  
);

CREATE TABLE cot_event
(
  id varchar(16) NOT NULL,
  source_id varchar(16) NOT NULL,
  cot_type varchar(16) NOT NULL,
  how varchar(16) NOT NULL,
  detail varchar(400) NOT NULL,
  servertime varchar(16) NOT NULL,
  CONSTRAINT cot_event_pkey PRIMARY KEY (id),
  CONSTRAINT source_pk FOREIGN KEY (source_id)
      REFERENCES source (source_id) MATCH SIMPLE
      ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE TABLE cot_event_position
(
  cot_event_id varchar(16) NOT NULL,
  point_hae varchar(16) NOT NULL,
  point_ce varchar(16) NOT NULL,
  point_le varchar(16) NOT NULL,
  tileX varchar(16) NOT NULL,
  tileY varchar(16) NOT NULL,
  longitude varchar(24) NOT NULL,
  latitude varchar(24) NOT NULL,
  CONSTRAINT cot_event_position_pkey PRIMARY KEY (cot_event_id),
  CONSTRAINT cot_event_fk FOREIGN KEY (cot_event_id)
      REFERENCES cot_event (id) MATCH SIMPLE
      ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE TABLE master_cot_event
(
  id varchar(16) NOT NULL,
  source_id varchar(16) NOT NULL,
  cot_type varchar(16) NOT NULL,
  how varchar(16) NOT NULL,
  detail varchar(400) NOT NULL,
  servertime varchar(16) NOT NULL,
  point_hae varchar(16) NOT NULL,
  point_ce varchar(16) NOT NULL,
  point_le varchar(16) NOT NULL,
  tilex varchar(16) NOT NULL,
  tiley varchar(16) NOT NULL,
  longitude varchar(24) NOT NULL,
  latitude varchar(24) NOT NULL,
  CONSTRAINT master_cot_event_pkey PRIMARY KEY (id),
  CONSTRAINT master_source_fk FOREIGN KEY (source_id)
      REFERENCES source (source_id) MATCH SIMPLE
      ON UPDATE RESTRICT ON DELETE RESTRICT
);
