SET search_path TO :default_schema; --Note default_schema is set in setup-baseline.sh when psql is invoked.

CREATE OR REPLACE FUNCTION COMPUTE_TILE()
RETURNS trigger AS
$$
DECLARE
  tilex integer;
  tiley integer;
BEGIN
	
  SELECT * FROM baseline.tilexy(NEW.longitude, NEW.latitude, 16) INTO tilex, tiley;
  NEW.TILEX = tilex;
  NEW.TILEY = tiley;
  RETURN NEW;
END;
$$
LANGUAGE 'plpgsql';

CREATE TRIGGER UPDATE_TILE BEFORE INSERT OR UPDATE OF longitude, latitude
   ON cot_event_position FOR EACH ROW
   EXECUTE PROCEDURE COMPUTE_TILE();