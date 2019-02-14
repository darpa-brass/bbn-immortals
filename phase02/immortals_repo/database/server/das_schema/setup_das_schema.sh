#!/bin/sh

set -e

echo "Creating empty das schema ..."

immortals_db="immortals"
immortals_user="immortals"
immortals_user_pwd="immortals"
schema="das"
source_path=`cd "$(dirname "$0")"; pwd`
parent_path=`cd "$source_path"; cd ..; pwd`

command="DROP SCHEMA IF EXISTS $schema CASCADE"
psql -a -d $immortals_db -c "$command"

command="CREATE SCHEMA $schema authorization $immortals_user;"
psql -a -d $immortals_db -c "$command"

#Note that this is an empty schema initially (exception for one empty analysis table). At analysis time, the DAS creates and populates
#tables as needed to represent training data. Each time the DAS jar is run with the --analyze option,
#it deletes the contents of this schema and recreates the needed object.

#The following table is created empty. It can be used to import the schemaEvolutionResults.csv file, which 
#records the results of the DAS schema evolution module. Note that this import is a manual process (e.g., use pgadmin3
#or run the COPY command using psql ... example in tak_schema\setup_tak_schema.sh file).
psql -a -d $immortals_db -v default_schema=$schema -f "$parent_path/das_schema/analysis_ddl.sql"