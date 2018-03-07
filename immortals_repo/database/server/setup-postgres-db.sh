#!/bin/sh

if [ "${1}" != "--unattended" ]; then

warning="
IMPORTANT: Running this file will create and populate the 'immortals' database on the local Postgres server. If a database named 'immortals' already exists, it will be deleted.

This script must be executed as the user created by Postgres during initial setup. On Linux, this is typically 'postgres'. Type 'sudo -u postgres ./setup-postgres-db.sh'. On OSX, this is the user ID you were logged in as at the time of initial setup.
 
Continue ('yes' or 'no')?: "

while true; do
	read -p "$warning" yn
	case $yn in
		[Yy]* ) break;;
		[Nn]* ) exit;;
		* ) echo "Type yes or no.";;	
	esac
done
fi

immortals_db="immortals"
immortals_user="immortals"
immortals_user_pwd="immortals"
baseline_schema="baseline"
source_path=`cd "$(dirname "$0")"; pwd`
das_schema="das"

command="DROP OWNED BY $immortals_user cascade;"
psql -a -c "$command"

command="DROP DATABASE IF EXISTS $immortals_db;"
psql -a -c "$command"

command="DROP USER IF EXISTS $immortals_user;"
psql -a -c "$command"

set -e

command="CREATE USER $immortals_user WITH PASSWORD '$immortals_user_pwd' SUPERUSER INHERIT CREATEDB NOCREATEROLE;"
psql -a -c "$command"

command="CREATE DATABASE $immortals_db WITH OWNER $immortals_user;"
psql -a -c "$command"

command="CREATE SCHEMA $baseline_schema authorization $immortals_user;"
psql -a -d $immortals_db -c "$command"

psql -a -d $immortals_db -v default_schema=$baseline_schema -f "$source_path/chunk_time.sql"
psql -a -d $immortals_db -v default_schema=$baseline_schema -f "$source_path/tilexy.sql"
psql -a -d $immortals_db -v default_schema=$baseline_schema -f "$source_path/baseline_schema_ddl.sql"

command="COPY $baseline_schema.source FROM '$source_path/source.csv' DELIMITER ',' CSV HEADER;"
psql -a -d $immortals_db -c "$command"

command="COPY $baseline_schema.cot_event FROM '$source_path/cot_event.csv' DELIMITER ',' CSV HEADER;"
psql -a -d $immortals_db -c "$command"

command="COPY $baseline_schema.cot_event_position FROM '$source_path/cot_event_position.csv' DELIMITER ',' CSV HEADER;"
psql -a -d $immortals_db -c "$command"

psql -a -d $immortals_db -v default_schema=$baseline_schema -f "$source_path/create_trigger_ddl.sql"


#Create DAS schema

command="CREATE SCHEMA $das_schema authorization $immortals_user;"
psql -a -d $immortals_db -c "$command"
