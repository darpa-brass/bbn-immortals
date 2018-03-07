1) The instructions below were completed within an Ubuntu 16 VirtualBox VM.
The instructions are specific to building the minimum Docker containers to run
castor (not the entire Immortals system). You can tweak these steps according to your needs
of course. They were also tested on mac docker. For mac docker, ensure you have enough memory dedicated to
the docker container (at 3 GB) for voltdb.

2) Navigate to trunk/harness/docker/. Build immortals docker image:

sudo docker build --tag=immortals_docker:latest immortals_docker

3) Build voltdb docker image

sudo docker build --tag=voltdb_docker:latest voltdb_docker

4) Build the castor image

sudo docker build --tag=castor_docker:latest castor_docker

5) Create and start a castor container:

docker run -it --privileged castor_docker:latest /bin/bash -l

6) Get the latest castor code:

cd /
svn co https://dsl-external.bbn.com/svn/immortals/trunk/castor

7) Start VoltDB database.

Go to castor/voltdb7 folder.
Run command: ant (add & if want to run on background)

8) Install unzip.

apt install unzip

9) Go to castor/takserver folder. Unzip data file:

unzip large_data_set.zip

10) Load takserver base schema. From the castor/takserver folder:

sh load-schema1-strings.sh

11) Stop VoltDB.

Go to castor/voltdb7 folder.

Run command: ant stop

12) You should save the container so you can use it again:

In a separate terminal, type: docker ps
This will show a list of docker containers that are running.

Commit the running container using the long name:

docker commit ad865b322d20 loadedcastor:latest

Replace 'ad8...' above with the value you see.

To run the container again in the future, type the following in a terminal:

