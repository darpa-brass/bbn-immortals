SET search_path TO :default_schema; --Note default_schema is set in bash script when psql is invoked.

CREATE OR REPLACE FUNCTION OrBool(lhs boolean, rhs boolean) RETURNS boolean AS $$ SELECT lhs OR rhs; $$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION EqualVc(lhs varchar(16), rhs varchar(16)) RETURNS boolean AS $$ SELECT lhs = rhs; $$ LANGUAGE SQL;