SET search_path TO :default_schema; --Note default_schema is set in setup_das_schema.sh when psql is invoked.

CREATE TABLE das.schema_evol_analysis
(
  submission_model text,
  castor_query character varying(2000),
  aql_query character varying(2000),
  castor_validation character varying(5),
  aql_validation character varying(5),
  adaption_required character varying(5),
  class_name character varying(100),
  original_sql character varying(2000),
  adaptation_identifier character varying(30)
);
  
