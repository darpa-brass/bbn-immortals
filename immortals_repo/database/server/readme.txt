Setup notes for Postgres database (tested on Ubuntu 16.04 LTS) and related setup for Immortals.

#################################
STEP 1: Install Postgres:
#################################

sudo apt-get update
sudo apt-get install postgresql

You do *not* need the postgis extensions to run TakServer or the adaptation tests, but you should
install them for future use on the project.

#################################
STEP 2: Set your user permissions
#################################

Before running this script, ensure your current user is in the postgres group:

Run these commands in a terminal:
 
sudo usermod -a -G postgres ${USER}
sudo -u postgres psql -a -c \"CREATE USER ${USER} SUPERUSER INHERIT CREATEDB CREATEROLE;\"
createdb


#################################
STEP 3: Run setup.sh
#################################

Change the current directory to database/server and run:

sudo -u postgres ./setup.sh
