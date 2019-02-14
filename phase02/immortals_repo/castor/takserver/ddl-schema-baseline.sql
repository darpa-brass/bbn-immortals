DROP TABLE cot_event_position IF EXISTS;
DROP TABLE cot_event IF EXISTS;
DROP TABLE source IF EXISTS;

CREATE TABLE source
(
 id integer NOT NULL,
 name character varying,
 channel integer NOT NULL,
 CONSTRAINT source_pkey PRIMARY KEY (id)
 );

CREATE TABLE cot_event
(
  id integer NOT NULL,
  source_id integer NOT NULL,
  cot_type character varying NOT NULL,
  how character varying NOT NULL,
  detail character varying NOT NULL,
  servertime bigint NOT NULL,
  CONSTRAINT cot_event_pkey PRIMARY KEY (id),
  CONSTRAINT source_pk FOREIGN KEY (source_id)
    REFERENCES public.source (id) MATCH SIMPLE
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
  longitude float NOT NULL,
  latitude float NOT NULL,
  CONSTRAINT cot_event_position_pkey PRIMARY KEY (cot_event_id),
  CONSTRAINT cot_event_fk FOREIGN KEY (cot_event_id)
     REFERENCES public.cot_event (id) MATCH SIMPLE
     ON UPDATE RESTRICT ON DELETE RESTRICT
);
