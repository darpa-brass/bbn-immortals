SET search_path TO :default_schema; --Note default_schema is set in setup-baseline.sh when psql is invoked.

CREATE SEQUENCE source_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE SEQUENCE cot_event_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE source
(
  id integer NOT NULL DEFAULT nextval('source_id_seq'::regclass),
  name character varying,
  channel integer NOT NULL DEFAULT floor(random()*(16)),
  CONSTRAINT source_pkey PRIMARY KEY (id)
);

CREATE TABLE cot_event
(
  id integer NOT NULL DEFAULT nextval('cot_event_id_seq'::regclass),
  source_id integer NOT NULL,
  cot_type character varying NOT NULL,
  how character varying NOT NULL,
  detail text NOT NULL,
  servertime bigint NOT NULL DEFAULT cast (to_char(chunk_time(current_timestamp, '5 minutes'), 'YYYYMMDDHH24MI') as bigint),
  CONSTRAINT cot_event_pkey PRIMARY KEY (id),
  CONSTRAINT source_pk FOREIGN KEY (source_id)
      REFERENCES source (id) MATCH SIMPLE
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