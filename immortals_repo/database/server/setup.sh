#!/bin/sh

set -e

if [ "${1}" != "--unattended" ]; then

warning="
####################################################################
IMPORTANT: This will destroy/recreate the local 'immortals' database.

Before running this script, ensure your current user is in the postgres group:

Run these commands in a terminal:
 
sudo usermod -a -G postgres ${USER}
sudo -u postgres psql -a -c \"CREATE USER ${USER} SUPERUSER INHERIT CREATEDB CREATEROLE;\"
createdb
####################################################################

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

source_path=`cd "$(dirname "$0")"; pwd`

echo "Unzipping data ..."

unzip -o "${source_path}/data/large_data_set.zip" -d "${source_path}/data"
unzip -o "${source_path}/data/master_cot_event.zip" -d "${source_path}/data"

"${source_path}/pgcommon/setup_postgres_db.sh"
"${source_path}/das_schema/setup_das_schema.sh"
"${source_path}/tak_schema/setup_tak_schema.sh"
"${source_path}/takrpt_schema/setup_takrpt_schema.sh"
"${source_path}/takrptbase_schema/setup_takrptbase_schema.sh"
"${source_path}/takrptaql_schema/setup_takrptaql_schema.sh"

