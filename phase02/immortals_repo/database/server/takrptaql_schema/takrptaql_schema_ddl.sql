SET search_path TO :default_schema; --Note default_schema is set in setup_takrptaql_schema.sh when psql is invoked.

CREATE TABLE source
(
  source_id integer NOT NULL,
  name character varying,
  channel integer NOT NULL,
  CONSTRAINT source_pkey PRIMARY KEY (source_id)
);

CREATE TABLE cot_event
(
  id integer NOT NULL,
  source_id integer NOT NULL,
  cot_type character varying NOT NULL,
  how character varying NOT NULL,
  detail text NOT NULL,
  servertime bigint NOT NULL,
  CONSTRAINT cot_event_pkey PRIMARY KEY (id),
  CONSTRAINT source_pk FOREIGN KEY (source_id)
      REFERENCES source (source_id) MATCH SIMPLE
      ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE TABLE cot_event_position
(
  cot_event_id integer NOT NULL,
  point_hae integer NOT NULL,
  point_ce integer NOT NULL,
  point_le integer NOT NULL,
  tileX integer NOT NULL,
  tileY integer NOT NULL,
  longitude double precision NOT NULL,
  latitude double precision NOT NULL,
  CONSTRAINT cot_event_position_pkey PRIMARY KEY (cot_event_id),
  CONSTRAINT cot_event_fk FOREIGN KEY (cot_event_id)
      REFERENCES cot_event (id) MATCH SIMPLE
      ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE TABLE master_cot_event
(
  id integer NOT NULL,
  source_id integer NOT NULL,
  cot_type character varying NOT NULL,
  how character varying NOT NULL,
  detail text NOT NULL,
  servertime bigint NOT NULL,
  point_hae integer NOT NULL,
  point_ce integer NOT NULL,
  point_le integer NOT NULL,
  tilex integer NOT NULL,
  tiley integer NOT NULL,
  longitude double precision NOT NULL,
  latitude double precision NOT NULL,
  CONSTRAINT master_cot_event_pkey PRIMARY KEY (id),
  CONSTRAINT master_source_fk FOREIGN KEY (source_id)
      REFERENCES source (source_id) MATCH SIMPLE
      ON UPDATE RESTRICT ON DELETE RESTRICT
);
