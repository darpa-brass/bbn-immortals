#!/bin/sh

echo "Creating immortals user and database ..."

immortals_db="immortals"
immortals_user="immortals"
immortals_user_pwd="immortals"

command="DROP OWNED BY $immortals_user cascade;"
psql -a -c "$command"

set -e

command="DROP DATABASE IF EXISTS $immortals_db;"
psql -a -c "$command"

command="DROP USER IF EXISTS $immortals_user;"
psql -a -c "$command"

command="CREATE USER $immortals_user WITH PASSWORD '$immortals_user_pwd' SUPERUSER INHERIT CREATEDB NOCREATEROLE;"
psql -a -c "$command"

command="CREATE DATABASE $immortals_db WITH OWNER $immortals_user;"
psql -a -c "$command"

