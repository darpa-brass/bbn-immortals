#!/bin/sh

set -e

immortals_db="immortals"
immortals_user="immortals"
immortals_user_pwd="immortals"
source_path=`cd "$(dirname "$0")"; pwd`
schema="immortals"

command="DROP SCHEMA IF EXISTS $schema CASCADE;"
psql -a -d $immortals_db -c "$command"

command="CREATE SCHEMA $schema authorization $immortals_user;"
psql -a -d $immortals_db -c "$command"

psql -a -d $immortals_db -v default_schema=$schema -f "$source_path/chunk_time.sql"
psql -a -d $immortals_db -v default_schema=$schema -f "$source_path/tilexy.sql"
psql -a -d $immortals_db -v default_schema=$schema -f "$source_path/immortals_schema0_ddl.sql"

command="COPY $schema.source FROM '$source_path/source.csv' DELIMITER ',' CSV HEADER;"
psql -a -d $immortals_db -c "$command"

command="COPY $schema.cot_event FROM '$source_path/cot_event.csv' DELIMITER ',' CSV HEADER;"
psql -a -d $immortals_db -c "$command"

command="COPY $schema.cot_event_position FROM '$source_path/cot_event_position.csv' DELIMITER ',' CSV HEADER;"
psql -a -d $immortals_db -c "$command"

command="COPY $schema.master_cot_event FROM '$source_path/master_cot_event.csv' DELIMITER ',' CSV HEADER;"
psql -a -d $immortals_db -c "$command"
