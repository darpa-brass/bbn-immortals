Setup notes for Postgres database (tested on Ubuntu 16):

1. Install Postgres:

sudo apt-get update
sudo apt-get install postgresql

You do *not* need the postgis extensions.

2. Setup the immortals database and the 'baseline' and 'immortals' schema. Both will be populated with the same
data used by the Castor researchers (though they are using voltdb). Castor-related files are located here: trunk/castor/

If you haven't done so already, create a local directory and check out the immortals files from the trunk:

svn co https://dsl-external.bbn.com/svn/immortals/trunk

Copy and expand the file trunk/castor/takserver/large_data_set.zip to the location: trunk/database/server.
If you are working with Castor, you need to also expand the file in its source location (trunk/castor/takserver).

Change to the trunk/database/server folder.

Execute the setup script:

sudo -u postgres ./setup-postgres-db.sh

This will create an immortals database with a schema called baseline. The tables will be loaded by the csv files you expanded earlier (the ones in large_data_set.zip).

3. At test time (at MIT LL), perturbed schema will be created. This perturbed schema will be called 'immortals' (instead of 'baseline'). The immortals tak server
application will run against this perturbed schema (all except the insert statements).

To create a 'degenerate' perturbed schema that is identical to the baseline, run the 'setup-immortals-schema0.sh' script contained in trunk/database/server.

