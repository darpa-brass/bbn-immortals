The DAS Java JAR manages the Immortals adaptation sequence. Follow these steps to run the DAS:

*1* Pull the DAS along with the other Immortals artifacts from subversion (best to pull the entire trunk). Be sure to maintain relative directory path locations.

*2* Build everything by typing the following in a terminal at the Immortals trunk folder:

gradle cleanall
gradle buildAll

*3* Start a Fuseki instance (after downloading fuseki, type '/fuseki-server --update --mem /ds' in the apache fuseki folder).

*4* Run the Securboration repository-service (from the /trunk/knowledge-repo/knowledge-repo/repository-service/target folder, type: 'java -Dserver.port=9999 -jar immortals-repository-service-boot.war'). If you don't see the war file, run the build process for the securboration tools (type mvn clean install in the knowledge-repo folder).

There is further documentation in the repository-service folder.

*5* From the das-service root folder, start the DAS by typing 'java -jar das.jar /Users/petersamouelian/Documents/Immortals/source/trunk'

    Note 1: The program argument provides the location of your Immortals trunk folder; adjust as appropriate for your local system.

    Note 2: DAS attempts to run two tools using the Java ProcessBuilder: stack and gradle. It relies on the JVM path being set properly. 

    On some environments (particularly in Eclipse on MacOS/X), you may need to pass this to the JVM as an environment property.

*6* The DAS exposes a REST endpoint. Once the DAS is running, you can pass it a new deployment model as follows. First, open a new terminal 
    separate from the das instance. Navigate to the das/das-service directory (or wherever your deployment model is). Type the following:

    curl -H "Content-Type:text/plain" -X POST --data-binary @gme-output-full.ttl http://localhost:8080/bbn/das/deployment-model

    Note: The deployment model is contained in sample_das_input.txt in the above example. This file is included with the checked in project.

You should see (in the das terminal) the output for the adaptation. Note that this will start a build of the AtakClient if a suitable adaptation is found.