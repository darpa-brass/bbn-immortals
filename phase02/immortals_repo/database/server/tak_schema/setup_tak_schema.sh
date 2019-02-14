#!/bin/sh

set -e

echo "Creating and populating tak operational schema ..."

immortals_db="immortals"
immortals_user="immortals"
immortals_user_pwd="immortals"
schema="tak"
source_path=`cd "$(dirname "$0")"; pwd`
parent_path=`cd "$source_path"; cd ..; pwd`

command="DROP SCHEMA IF EXISTS $schema CASCADE;"
psql -a -d $immortals_db -c "$command"

command="CREATE SCHEMA $schema authorization $immortals_user;"
psql -a -d $immortals_db -c "$command"

psql -a -d $immortals_db -v default_schema=$schema -f "$parent_path/pgcommon/chunk_time.sql"
psql -a -d $immortals_db -v default_schema=$schema -f "$parent_path/pgcommon/tilexy.sql"
psql -a -d $immortals_db -v default_schema=$schema -f "$source_path/tak_schema_ddl.sql"

command="COPY $schema.source FROM '$parent_path/data/source.csv' DELIMITER ',' CSV HEADER;"
psql -a -d $immortals_db -c "$command"

command="COPY $schema.cot_event FROM '$parent_path/data/cot_event.csv' DELIMITER ',' CSV HEADER;"
psql -a -d $immortals_db -c "$command"

command="COPY $schema.cot_event_position FROM '$parent_path/data/cot_event_position.csv' DELIMITER ',' CSV HEADER;"
psql -a -d $immortals_db -c "$command"

psql -a -d $immortals_db -v default_schema=$schema -f "$parent_path/pgcommon/create_trigger_ddl.sql"