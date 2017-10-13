SET search_path TO :default_schema; --Note default_schema is set in setup-baseline.sh when psql is invoked.

CREATE FUNCTION chunk_time(IN raw_timestamp timestamptz, chunk_interval INTERVAL) 
RETURNS timestamptz AS $$
	SELECT TO_TIMESTAMP((EXTRACT(epoch FROM $1)::INTEGER + EXTRACT(epoch FROM $2)::INTEGER / 2)
                / EXTRACT(epoch FROM $2)::INTEGER * EXTRACT(epoch FROM $2)::INTEGER)
$$ LANGUAGE SQL STABLE;
