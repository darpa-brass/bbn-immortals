The TakServerDataManager executes queries against the Immortals TakServer database. To use this library, follow these steps.

These specific steps have been tested on Ubuntu 16 LTS.

Step 1: Install Postgres:

sudo apt-get update
sudo apt-get install postgresql

Make sure postgres is running when you run the following steps.

Step 2: If you haven't done so already, create a local directory and check out the immortals files from the trunk as follows:

svn co https://dsl-external.bbn.com/svn/immortals/trunk

Step 3: Copy and unzip the data files (used to populate database) to the right location by running these commands relative to the root of your immortals source code:

[change directory to parent of your immortals trunk folder]
cp trunk/castor/takserver/large_data_set.zip trunk/database/server/
unzip trunk/database/server/large_data_set.zip -d trunk/database/server/

Step 4: Change to the trunk/database/server folder.

Execute the setup script (NOTE: you may need to make the script executable using chmod u+x filename).

sudo -u postgres ./setup-postgres-db.sh

This will create an immortals database with a schema called baseline.
You may see this error (which you can ignore): ERROR:  role "immortals" cannot be dropped because some objects depend on it.

Step 5: Setup immortals schema.

sudo -u postgres ./setup-immortals-schema0.sh

This will create the perturbable version of the baseline schema.

Step 6: Prepare local build environment.

IIMPORTANT NOTE: Immortals requires many libraries. You can try to skip this step and jump to 7, but
if you are missing certain libraries locally (e.g., python, Android, etc.), you may see errors.
If you see errors, you should run trunk/harness/prepare_setup.sh and follow the instructions
in the root README.md file and harness/README-setup.md (the second file explains the finer details of the environment setup).

Step 7: Make sure you have built the entire Immortals project at least once before as follows:

./trunk/gradlew buildAll

Step 8: Build the TakServerDataManager jar. From the immortals root folder, type:

./trunk/gradlew -b trunk/shared/modules/dfus/TakServerDataManager/build.gradle fatJar

Step 9: Run the java main class (this executes all java methods in class).

Note: First, change directory to the immortals/shared/modules/dfus/TakServerDataManager/build/libs

java -cp ./TakServerDataManager-all-1.0-LOCAL.jar mil.darpa.immortals.dfus.TakServerDataManager.DataManager

You will see output like the following (ignore logging errors):

Running cotEventsForConstantChannelJoin
Running cotEventsForConstantChannelJoin2
Running cotEventsForConstantCompoundFilter
Running cotEventsForConstantMixedJoin
Running cotEventsForConstantTimeInterval
Running cotEventsForUidAndInterval
Running cotEventsOnChannelInRegion
Running cotEventsForConstantCotType

NOTE: JUnit tests are also checked in (see DataManagerTest.java) class in the project.
