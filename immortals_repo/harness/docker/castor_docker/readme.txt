1) The instructions below were completed within an Ubuntu 16 VirtualBox VM.
The instructions are specific to building the minimum Docker containers to run
castor (not the entire Immortals system). You can tweak these steps according to your needs
of course.

2) Navigate to trunk/harness/docker/. Build immortals docker image:

sudo docker build --tag=immortals_docker:latest immortals_docker

2) Build voltdb docker image

sudo docker build --tag=voltdb_docker:latest voltdb_docker

3. Build the castor image

sudo docker build --tag=castor_docker:latest castor_docker

4. Create and start a castor container:

docker run -it --privileged castor_docker:latest /bin/bash -l

5. Get the latest castor code:

cd /
svn co https://dsl-external.bbn.com/svn/immortals/trunk/castor

5) Start VoltDB database.

Go to castor/voltdb7 folder.
Run command: ant (add & if want to run on background)

6) Load takserver base schema:

Go to castor/takserver folder.
Run command to create takserver schema and load data: sh load-schema1.sh

7) Stop VoltDB.

Go to Castor/voltdb7 folder.
Run command: ant stop


