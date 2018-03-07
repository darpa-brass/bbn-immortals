SET search_path TO das;

DROP TABLE IF EXISTS query_definition CASCADE;
DROP TABLE IF EXISTS training_data CASCADE;
DROP TABLE IF EXISTS param_binding CASCADE;

DROP SEQUENCE IF EXISTS query_definition_id_seq CASCADE;
DROP SEQUENCE IF EXISTS training_data_id_seq CASCADE;
DROP SEQUENCE IF EXISTS param_binding_id_seq CASCADE;

CREATE SEQUENCE query_definition_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE SEQUENCE training_data_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE SEQUENCE param_binding_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;


CREATE TABLE query_definition
(
  query_definition_id integer NOT NULL DEFAULT nextval('query_definition_id_seq'::regclass),
  query_name character varying,
  source_sql character varying,
  number_params integer NOT NULL,
  CONSTRAINT query_definition_pkey PRIMARY KEY (query_definition_id)
);

CREATE TABLE training_data
(
   training_data_id integer NOT NULL DEFAULT nextval('training_data_id_seq'::regclass),
   query_definition_id integer NOT NULL,
   table_name character varying,
   training_type char(1) NOT NULL CHECK(training_type = 'P' or training_type = 'N'),
   CONSTRAINT training_data_pkey PRIMARY KEY (training_data_id),
   CONSTRAINT query_definition_fk FOREIGN KEY (query_definition_id)
      REFERENCES query_definition (query_definition_id) MATCH SIMPLE
      ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE TABLE param_binding
(
   param_binding_id integer NOT NULL DEFAULT nextval('param_binding_id_seq'::regclass),
   training_data_id integer NOT NULL,
   ordinal_position integer NOT NULL,
   value character varying,
   CONSTRAINT param_binding_pkey PRIMARY KEY (param_binding_id),
   CONSTRAINT training_data_fk FOREIGN KEY (training_data_id)
      REFERENCES training_data (training_data_id) MATCH SIMPLE
      ON UPDATE RESTRICT ON DELETE RESTRICT
);
