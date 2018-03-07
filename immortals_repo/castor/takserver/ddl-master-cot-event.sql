DROP TABLE master_cot_event IF EXISTS;

CREATE TABLE master_cot_event
(
  id VARCHAR(16) NOT NULL,
  source_id VARCHAR(16) NOT NULL,
  cot_type VARCHAR(16) NOT NULL,
  how VARCHAR(16) NOT NULL,
  detail VARCHAR(400) NOT NULL,
  servertime VARCHAR(16) NOT NULL,
  point_hae VARCHAR(16) NOT NULL,
  point_ce VARCHAR(16) NOT NULL,
  point_le VARCHAR(16) NOT NULL,
  tileX VARCHAR(16) NOT NULL,
  tileY VARCHAR(16) NOT NULL,
  longitude VARCHAR(24) NOT NULL,
  latitude VARCHAR(24) NOT NULL,
  CONSTRAINT master_cot_event_pkey PRIMARY KEY (id),
  CONSTRAINT master_source_pk FOREIGN KEY (source_id)
    REFERENCES public.source (id) MATCH SIMPLE
    ON UPDATE RESTRICT ON DELETE RESTRICT
);
