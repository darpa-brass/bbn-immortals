The TakServerDataManager executes queries against the Immortals TakServer database. To use this library, follow these steps on a linux machine:

1. Install Postgres:

sudo apt-get update
sudo apt-get install postgresql

Make sure postgres is running when you run the following steps.

2. Setup the immortals database and baseline schema, including the same data used by OSU and the voltdb:

If you haven't done so already, create a local directory and check out the immortals files from the trunk:

svn co https://dsl-external.bbn.com/svn/immortals/trunk

Change to the trunk/database/server folder.

Execute the setup script:

sudo -u postgres ./setup-postgres-db.sh

This will create an immortals database with a schema called baseline.

sudo -u immortals ./setup-immortals-schema0.sh

This will create the perturbable version of the baseline.

3. Build this jar file:

From the immortals root folder, type:

./gradlew -b shared/modules/dfus/TakServerDataManager/build.gradle fatJar

4. Run the java main class (this executes all java methods in class).

Note: First, change directory to the immortals/shared/modules/dfus/TakServerDataManager/build/libs

java -cp ./TakServerDataManager-all-1.0-LOCAL.jar mil.darpa.immortals.dfus.TakServerDataManager.DataManager

You can also invoke the test scripts during build using Gradle.