SET search_path TO :default_schema; --Note default_schema is set in setup-baseline.sh when psql is invoked.

CREATE OR REPLACE FUNCTION tilexy(IN longitude double precision, IN latitude double precision, IN zoom integer, OUT tileX integer, OUT tileY integer) AS $$
BEGIN
	tileX := floor((longitude + 180)/360 * (1<<zoom));
	tileY := floor( (1 - ln(tan(radians(latitude)) + 1 / cos(radians(latitude))) / pi()) / 2 * (1<<zoom) );

	if (tileX < 0) then
	  tileX := 0;
	end if;
	
	if tileX >= (1<<zoom) then
	  tileX := ((1<<zoom)-1);
	end if;

	if tileY < 0 then
	  tileY := 0;
	end if;
	
	if tileY >= (1<<zoom) then
	  tileY := ((1<<zoom)-1);
	end if;
END;
$$
LANGUAGE 'plpgsql' IMMUTABLE;
