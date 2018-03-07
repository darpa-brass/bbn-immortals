Setup notes for Postgres database (tested on Ubuntu 16.04 LTS)

1. Install Postgres:

sudo apt-get update
sudo apt-get install postgresql

You do *not* need the postgis extensions to run TakServer or the adaptation tests; postgis was used
for the initial creation of the sample data, but the results of this data are checked in as csv files here:

\castor\takserver\large_data_set.zip
\castor\takserver\master_cot_event.zip

2. Unzip the above zip files to two locations:

trunk\castor\takserver
trunk\database\server

... so in each location, you want to do:

unzip large_data_set.zip
unzip master_cot_data.zip

3. Setup the immortals database and the 'baseline' and 'immortals' schema. Both will be populated with the same
data used by the Castor researchers (though they are using voltdb).

Change to the trunk/database/server folder. Execute the setup script:

sudo -u postgres ./setup-postgres-db.sh

This will create an immortals database with a schema called baseline. The tables will be loaded by the zip 
files you expanded earlier.

4. At test time (at MIT LL), perturbed tables will be created in a separate schema called 'immortals'.
The immortals tak server 'select' queries will run against this perturbed 
schema (all except the insert statements, which are run against the baseline schema). This setup
simulates an operational database (the 'baseline' schema) versus a read-only, reporting database (the 'immortals' schema).
This separation supports Castor's range/scope as a SQL repair tool (i.e., it does not address dml statements). This is 
a fundamental limitation of relational learning but also of code repair strategies.

Create the 'immortals' schema now. Make sure you are in trunk/database/server and run:

sudo -u postgres ./setup-immortals-schema0.sh

This will create a perturbed schema that is identical to the baseline. It also creates a populated helper table that
makes it easier to create and load the perturbed tables at test time.

5. Notes

NOTE: You DO NOT need to run other scripts in this folder (database/server). The other scripts are either
invoked from the two .sh scripts you ran above or are not needed for setup.

For reference, the create_training_data.sh was used to create the training data for Castor, but the results 
of this script are checked in to the castor project so it is not necessary to run this script going forward
(unless you want to re-gen the training data (i.e., after changing/adding SQL scripts, for example).