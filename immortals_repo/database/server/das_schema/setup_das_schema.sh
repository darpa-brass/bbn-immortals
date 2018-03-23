#!/bin/sh

set -e

echo "Creating empty das schema ..."

immortals_db="immortals"
immortals_user="immortals"
immortals_user_pwd="immortals"
schema="das"

command="DROP SCHEMA IF EXISTS $schema CASCADE"
psql -a -d $immortals_db -c "$command"

command="CREATE SCHEMA $schema authorization $immortals_user;"
psql -a -d $immortals_db -c "$command"

#Note that this is an empty schema initially. At analysis time, the DAS creates and populates
#tables as needed to represent training data. Each time the DAS jar is run with the --analyze option,
#it deletes the contents of this schema and recreates the needed object.
