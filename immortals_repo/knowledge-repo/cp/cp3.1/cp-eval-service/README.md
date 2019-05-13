# Schema Evolution (CP3.1) Evaluation Harness
## Purpose of this document
This document describes how to use the CP3.1 Evaluation Harness.

## Description of the Evaluation Harness
The Evaluation Harness is a standalone executable JAR that orchestrates various operations needed to determine how well IMMoRTALS adapts an existing Exemplar Software System (ESS) to evolutionary pressure that manifests as schema changes within the ESS.  The Evaluation Harness orchestrates dozens of processes during an evaluation workflow.  At a high level, the Evaluation harness operates on the following inputs:
  * An evaluation configuration, typically retrieved from OrientDB (though it may also be specified manually for testing/debugging)
  * An Exemplar Software System (ESS) project template

If all goes well, the harness will emit a report that describes the impact of adaptation on the ESS when faced by the evolutionary pressure specified in the evaluation configuration.  Note that the overall evaluation workflow may take several minutes to complete.

## Description of the Exemplar Software System (ESS)

The ESS is a simple client/server architecture implemented using Java/Spring-Boot in which a client reads XML data from a local directory, processes the XML internally, transmits the XML to the server and receives an XML response from the server.  Evolutionary pressure arises when 
  1) Documents read by the client from the datasource (a directory on the file system) are non-compliant with the version for which the client was explicitly coded.
  2) Documents transmitted to the server from the client are non-compliant with the version expected by the server.
  3) Documents received from the server by the client are non-compliant with the version expected by the client.

The dataflows along which adaptation may need to occur are called out in numbered edges below:
```
                                  SOAP/XML
                                  messages
                      /--------\             /--------\ 
          XML messages|        | ====(2a)==> |        |  
[documents] --(1*)--> | CLIENT |             | SERVER |
                      |        | <==(2b)==== |        |
                      \--------/             \--------/

```
Note that edge 1* is not currently a degree of freedom and will provide no evolutionary pressure since the datasource and client schema versions will always be the same.  This constraint may change in the future.

## Evaluation environment
This guide makes the following assumptions about the evaluation environment:
* Operating System: Ubuntu Linux Server 64-bit 16.04.5 (minimal install)  
* CPU Cores: at least 2 (recommend 4)
* Memory: at least 12 GB (recommend 16GB)
* Executing User: Non-Root

Installed Components:

| Component      | Version | Exports     | Path Binaries |
|:---------------|:-------:|:------------|:--------------|
| Java (OpenJDK) | 1.8     | JAVA_HOME   | _Standard_    |
| Maven          | 3.3.9   | _None_      | "mvn"         |
| Python         | 3.5.2   | _None_      | "python3.5"   |
| Fuseki         | 2.3.1   | FUSEKI_HOME | _None_        |

The XML schema translation service (which is started automatically during an evaluation workflow) requires that you set up a Python environment.  The recommended mechanism is using Anaconda:
 1) Download/install Anaconda 3.7 from https://www.anaconda.com/distribution/#download-section
 2) Run the command: `conda env create -f environment.yml` from the directory `trunk/knowledge-repo/cp/cp3.1/xsd-tranlsation-service-aql/aql/`
 3) The server can thereafter be launched from that directory on port 8090 via `python server.py`.  Note that there is no need to do this prior to evaluation (the evaluation workflow starts the service for you) but it may be useful to test this before running a workflow.


## Building the Evaluation Harness
Part of a simple build script is shown below:
```
#...

#NOTE: update an IMMoRTALS code repo previously checked out to directory ./trunk
svn update trunk

#build OrientDB adapter
./trunk/phase3/mdl-schema-evolution/gradlew publishToMavenLocal

#build knowledge repo modules
mvn -f ./trunk/knowledge-repo/pom.xml clean install -DskipTests

#build XSD translation service
mvn -f ./trunk/knowledge-repo/cp/cp3.1/xsd-translation-service/pom.xml clean install -DskipTests

#NOTE: the harness WILL NOT WORK if any of the build steps above fail
```

In the above example, the evaluation harness executable JAR is emitted to ./trunk/knowledge-repo/cp/cp3.1/cp-eval-service/target/immortals-cp3.1-eval-service-boot.jar


## Running the Evaluation Harness

The general form of an evaluation run command looks like 

```java -Dkey1=value1 -Dkey2=value2 ... -jar cp3.1-eval-service.jar```

An example of a simple script that launches the Evaluation Harness in 'testSanity' mode (the default) is shown below:
```
# launch the executable JAR from . (IMMoRTALS codebase checked out and built under ./trunk)
java -DpathToXsdTranslationServiceJar=./trunk/knowledge-repo/cp/cp3.1/xsd-translation-service/target/immortals-xsd-translation-service-boot.jar \
 -DfusekiHome=/home/jacob/Desktop/fuseki/apache-jena-fuseki-2.3.1 \
 -Dserver.port=8088 \
 -DessTemplateDir=./trunk/knowledge-repo/cp/cp3.1/cp-ess-min \ -DpathToInstrumentationJar=./trunk/knowledge-repo/cp/cp3.1/etc/rampart.jar \
 -DdomainKnowledge=./trunk/knowledge-repo/cp/cp3.1/etc/arch.ttl \
 -jar cp3.1-eval-service.jar
```

### Evaluation modes
The harness can be run in one of the following test modes (useful for debugging, exploration and replay):
  * <i>testSanity</i>: the harness will perform an evaluation with no evolutionary pressure
  * <i>testSanity1</i>: the harness will perform an evaluation with a v1 client and datasource and a v2 server
  * <i>testSanity2</i>: the harness will perform an evaluation with a v2 client and datasource and a v1 server  
  * <i>testCanned</i>: the harness will perform an evaluation using a canned JSON input specified via the cannedInputJson property (this mode is useful for replaying problematic configurations)
  * <i>testSimple</i>: the harness will perform an evaluation using a configuration captured by the simpleClientSchemaVersion, simpleServerSchemaVersion, and simpleDatasourceSchemaVersion properties.
  * <i>testComplex</i>: the harness will perform an evaluation using a configuration captured by the customDirContainingClientXsds, customDirContainingServerXsds, customDirContainingDatasourceXsds, and customDirContainingDatasourceXml properties.

The following non-test evaluation modes are provided:
  * <i>live</i>: the harness will retrieve the evaluation configuration JSON from OrientDB

### Configuration
#### Summary of key configuration properties

|key|default|desc|
|:---|:---|:---|
|evalType|testSanity|The sort of evaluation to perform (one of 'testSanity', 'testCanned', 'testSimple', 'testCustom', or 'live').|
|essTemplateDir|null|For a 'testSimple' or 'testSanity' evaluation type, the path to a template cp-ess-min module.  Ignored for other evaluation types.|
|fusekiHome|null|A directory containing fuseki 2.3.1.  The default value of this property is derived from the ${FUSEKI_HOME} environment variable, though it may be overridden.|
|pathToXsdTranslationServicePy|null|The path to the XSD translation service, if it is implemented in python/flask.  The default is null.|
|pathToInstrumentationJar|null|The path to a JAR that instruments bytecode as part of a dynamic analysis workflow.  The default is null.|
|evalOutputDir|./eval-out|The path to a directory where values emitted during evaluation will be dumped.|
|domainKnowledge|null|A TTL file containing the human-generated knowledge needed to perform adaptation.|

#### Description of all configuration properties
|key|default|desc|
|:---|:---|:---|
|evalType|testSanity|The sort of evaluation to perform (one of 'testSanity', 'testCanned', 'testSimple', 'testCustom', or 'live').   The default value is "testSanity" with the other types exposing increasing degrees of freedom (and complexity) to the evaluator.   A 'sanity' run will never experience evolutionary pressure and is therefore useful for testing whether the harness can run at all in your environment (a 'testSanity' run requires the bare minimum of configuration parameters).   A 'testCanned' run accepts a JSON document as input that specifies the various schema versions at play--the benefit being that there is no chance of accidentally misconfiguring the CP because it will have been vetted a priori by the performer.   A 'testSimple' run exposes more freedom to the evaluator in that the evaluator can select from several predefined schemas and document sets to use for the client, server, and datasource.  As in the 'testCanned' case, these configurations should all be valid out of the box (the difference is that it takes more work to configure a simple run than a canned one).   A 'testCustom' run exposes the full evaluation space to evaluators and care must therefore be taken to ensure that the configuration is sensible (e.g., providing a datasource document set that does not comply with the specified datasource schema would be nonsensical).   A 'live' run is similar to 'testCustom' with the notable difference that the harness will retrieve the evaluation configuration from OrientDB instead of from configuration properties.|
|cannedInputJson|null|For a 'testCanned' evaluation type, the path to an approved JSON configuration file. This value is ignored for other evaluation types.|
|simpleClientSchemaVersion|null|For a 'testSimple' evaluation type, a tag that uniquely identifies the schema version used by the client. If evalType is not 'testSimple', this value is ignored. 'v1' = MDL 17, 'v2' = MDL 19.|
|simpleServerSchemaVersion|null|For a 'testSimple' evaluation type, a tag that uniquely identifies the schema version used by the server. If evalType is not 'testSimple', this value is ignored. 'v1' = MDL 17, 'v2' = MDL 19.|
|simpleDatasourceSchemaVersion|null|For a 'testSimple' evaluation type, a tag that uniquely identifies the schema version used by the datasource. If evalType is not 'testSimple', this value is ignored. 'v1' = MDL 17, 'v2' = MDL 19.|
|essTemplateDir|null|For a 'testSimple' or 'testSanity' evaluation type, the path to a template cp-ess-min module.  Ignored for other evaluation types.|
|customDirContainingDatasourceXsds|null|For a 'testCustom' evaluation type, a directory containing .xsd files that collectively define the schema version used by the datasource. If evalType is not 'testCustom', this value is ignored.|
|customDirContainingClientXsds|null|For a 'testCustom' evaluation type, a directory containing .xsd files that collectively define the schema version used by the client. If evalType is not 'testCustom', this value is ignored.|
|customDirContainingServerXsds|null|For a 'testCustom' evaluation type, a directory containing .xsd files that collectively define the schema version used by the server. If evalType is not 'testCustom', this value is ignored.|
|customDirContainingDatasourceXml|null|For a 'testCustom' evaluation type, a directory containing XML documents conformant to the schema version used by the datasource. If evalType is not 'testCustom', this value is ignored.|
|cheatDir|null|The path to a directory that is merged into the working eval directory after creation.  This can be a powerful way to override certain aspects of the evaluation workflow, but use with extreme caution.|
|fusekiHome|null|A directory containing fuseki 2.3.1.  The default value of this property is derived from the ${FUSEKI_HOME} environment variable, though it may be overridden.|
|pathToXsdTranslationServiceJar|null|The path to the XSD translation service, if it is implemented as a standalone executable JAR.  The default is null.|
|pathToXsdTranslationServicePy|null|The path to the XSD translation service, if it is implemented in python/flask.  The default is null.|
|pathToInstrumentationJar|null|The path to a JAR that instruments bytecode as part of a dynamic analysis workflow.  The default is null.|
|evalOutputDir|./eval-out|The path to a directory where values emitted during evaluation will be dumped.|
|uniqueDirPerEval|false|A boolean that if true will force a unique output directory (rooted in the eval output dir) to be used per evaluation run.  This may result in excessive disk use for a large number of runs.|
|includeWorkflowDetailsInReport|false|A boolean that if true will result in fine-grained information about the evaluation workflow being inserted into the evaluation report.|
|hostName|localhost|If for some reason you need to refer to this machine by a name other than "localhost" in an HTTP request, specify it here|
|pythonExecutable|python3.5|The name of a Python interpreter on the current path OR the path to a Python interpreter not on the current path.|
|javaExecutable|java|The name of a JVM executable on the current path OR the path to a JVM executable not on the path.|
|domainKnowledge|null|A TTL file containing the human-generated knowledge needed to perform adaptation.|

### Smoke testing the evaluation harness
Assuming the IMMoRTALS repository is checked out in directory `trunk`, the canned json configurations for use in testCanned mode can be found in `trunk/knowledge-repo/cp/cp3.1/cp-eval-service/examples`.  Below are several snippets from scripts that run the harness under different configurations:

#### perform a sanity check evaluation run with no evolutionary pressure
```
# copy the eval harness JAR into . 
cp ../immortals-code/trunk/knowledge-repo/cp/cp3.1/cp-eval-service/target/immortals-cp3.1-eval-service-boot.jar cp3.1-eval-service.jar

# launch the executable JAR with no evolutionary pressure
java -D"evalType=testCanned" \
 -D"cannedInputJson=../immortals-code/trunk/knowledge-repo/cp/cp3.1/cp-eval-service/examples/sanityCheck.json" \
 -D"pathToXsdTranslationServicePy=../immortals-code/trunk/knowledge-repo/cp/cp3.1/xsd-tranlsation-service-aql/aql/server.py" \
 -D"xsdTranslationServicePort=8090" \
 -D"pythonExecutable=python" \
 -D"fusekiHome=/home/jacob/Desktop/fuseki/apache-jena-fuseki-2.3.1" \
 -D"server.port=8088" \
 -D"essTemplateDir=../immortals-code/trunk/knowledge-repo/cp/cp3.1/cp-ess-min" \
 -D"pathToInstrumentationJar=../immortals-code/trunk/knowledge-repo/cp/cp3.1/etc/rampart.jar" \
 -D"domainKnowledge=../immortals-code/trunk/knowledge-repo/cp/cp3.1/cp-ess-min/etc/arch.ttl" \
 -jar cp3.1-eval-service.jar
```

#### perform an evaluation run with manually applied evolutionary pressure provided by a canned configuration
```
# copy the eval harness JAR into . 
cp ../immortals-code/trunk/knowledge-repo/cp/cp3.1/cp-eval-service/target/immortals-cp3.1-eval-service-boot.jar cp3.1-eval-service.jar

# launch the executable JAR with evolutionary pressure (client,datasource=v17, server=v19)
java -D"evalType=testCanned" \
 -D"cannedInputJson=../immortals-code/trunk/knowledge-repo/cp/cp3.1/cp-eval-service/examples/client_v17-server_v19-datasource_v17.json" \
 -D"pathToXsdTranslationServicePy=../immortals-code/trunk/knowledge-repo/cp/cp3.1/xsd-tranlsation-service-aql/aql/server.py" \
 -D"xsdTranslationServicePort=8090" \
 -D"pythonExecutable=python" \
 -D"fusekiHome=/home/jacob/Desktop/fuseki/apache-jena-fuseki-2.3.1" \
 -D"server.port=8088" \
 -D"essTemplateDir=../immortals-code/trunk/knowledge-repo/cp/cp3.1/cp-ess-min" \
 -D"pathToInstrumentationJar=../immortals-code/trunk/knowledge-repo/cp/cp3.1/etc/rampart.jar" \
 -D"domainKnowledge=../immortals-code/trunk/knowledge-repo/cp/cp3.1/cp-ess-min/etc/arch.ttl" \
 -jar cp3.1-eval-service.jar
```

#### evaluate using the configuration stored in OrientDB
```
# copy the eval harness JAR into . 
cp ../immortals-code/trunk/knowledge-repo/cp/cp3.1/cp-eval-service/target/immortals-cp3.1-eval-service-boot.jar cp3.1-eval-service.jar

# launch the executable JAR with the configuration retrieved from OrientDB
java -D"evalType=live" \
 -D"cannedInputJson=../immortals-code/trunk/knowledge-repo/cp/cp3.1/cp-eval-service/examples/sanityCheck.json" \
 -D"pathToXsdTranslationServicePy=../immortals-code/trunk/knowledge-repo/cp/cp3.1/xsd-tranlsation-service-aql/aql/server.py" \
 -D"xsdTranslationServicePort=8090" \
 -D"pythonExecutable=python" \
 -D"fusekiHome=/home/jacob/Desktop/fuseki/apache-jena-fuseki-2.3.1" \
 -D"server.port=8088" \
 -D"essTemplateDir=../immortals-code/trunk/knowledge-repo/cp/cp3.1/cp-ess-min" \
 -D"pathToInstrumentationJar=../immortals-code/trunk/knowledge-repo/cp/cp3.1/etc/rampart.jar" \
 -D"domainKnowledge=../immortals-code/trunk/knowledge-repo/cp/cp3.1/cp-ess-min/etc/arch.ttl" \
 -jar cp3.1-eval-service.jar
```

Note that the configuration stored in OrientDB and used during a `live` evaluation is different (smaller and simpler) than the one used during testing.  The structure of an OrientDB configuration will look like the following:
```
{
    "initialMdlVersion": "V0_8_17",
    "updatedMdlVersion": "V0_8_19"
}
```

where `initialMdlVersion` and `updatedMdlVersion` are one of {`V0_8_17`,`V0_8_19`}.  Another form of configuration JSON is possible, this one permitting the use of an arbitrary schema:

```
{
    "initialMdlVersion": "V0_8_17",
    "updatedMdlSchema": "TODO: a very long literal string containing a flattened XML schema goes here"
}
```

Note that when the harness encounters a configuration that includes both `updatedMdlVersion` and `updatedMdlSchema`, it will ignore the former.  For example, the configuration below would not result in evolutionary pressure:
```
{
    "initialMdlVersion": "V0_8_17",
    "updatedMdlVersion": "V0_8_19", /*  this gets overridden because updateMdlSchema is not null!  */
    "updatedMdlSchema": "TODO: V0_8_17 XML schema goes here"
}
```

## Interpreting evaluation results
The evaluation harness emits its output to the directory specified by evalOutputDir (default is ./eval-out), which will contain the following:
* results.json: a document containing metrics that describe the effectiveness and cost of adaptation
* evaluation-config.json: a copy of the actual configuration used (this can later be used as a canned configuration input to replay a problematic evaluation scenario)
* evaluationArchive.zip: contains fine-grained information about the evaluation run
  * ess-orig.zip: a nested zip snapshot of the state of the ESS before adaptation was performed
  * eval.request.json: a copy of the evaluation request received internally by the evaluation server
  * eval.stderr: the stderr stream of the evaluation workflow
  * eval.stdout: the stdout stream of the evaluation workflow
    * ess
      * command-x-of-y-(desc): contains the stdout/stderr streams of each command x executed as part of the evaluation workflow.
      * ess: contains an archive of the ESS after analysis and adaptation were performed

	  
### Example outputs

#### An example result (no evolutionary pressure)
Below is the output of a sanity check run (no evolutionary pressure).  Note that the "impactOfAdaptation" values in the metrics belonging to the "impacts of adaptation" category are unchanged or trivially changed because nothing is broken in this scenario.

```
{
  "evaluationStatus" : "COMPLETED_OK",
  "evaluationDetails" : null,
  "categories" : [ {
    "categoryDesc" : "metrics relevant to evaluation workflow",
    "metricsForCategory" : [ {
      "metricType" : "eval context ID",
      "metricDesc" : "a unique identifier for the evaluation context",
      "metricValue" : "421e3043-195f-4196-9f10-50ccd90d5124"
    }, {
      "metricType" : "eval start time",
      "metricDesc" : "the starting time of the evaluation run",
      "metricValue" : "Mon Feb 04 11:27:19 EST 2019"
    }, {
      "metricType" : "eval end time",
      "metricDesc" : "the ending time of the evaluation run",
      "metricValue" : "Mon Feb 04 11:30:56 EST 2019"
    }, {
      "metricType" : "eval elapsed millis",
      "metricDesc" : "the duration of the evaluation run in milliseconds",
      "metricValue" : "216307"
    }, {
      "metricType" : "eval # cores",
      "metricDesc" : "the number of cores available to the JVM performing evaluation",
      "metricValue" : "8"
    }, {
      "metricType" : "eval max memory",
      "metricDesc" : "the memory available to the JVM performing evaluation",
      "metricValue" : "7537164288"
    } ]
  }, {
    "categoryDesc" : "impacts of adaptation",
    "metricsForCategory" : [ {
      "metricType" : "server ping (millis)",
      "metricDesc" : "a comparison of \"server ping (millis)\" before and after adaptation",
      "metricValueBefore" : "5",
      "impactOfAdaptation" : "unchanged after adaptation",
      "metricValueAfter" : "5"
    }, {
      "metricType" : "# tests performed",
      "metricDesc" : "a comparison of \"# tests performed\" before and after adaptation",
      "metricValueBefore" : "48",
      "impactOfAdaptation" : "unchanged after adaptation",
      "metricValueAfter" : "48"
    }, {
      "metricType" : "# tests that passed",
      "metricDesc" : "a comparison of \"# tests that passed\" before and after adaptation",
      "metricValueBefore" : "48",
      "impactOfAdaptation" : "unchanged after adaptation",
      "metricValueAfter" : "48"
    }, {
      "metricType" : "# tests that failed",
      "metricDesc" : "a comparison of \"# tests that failed\" before and after adaptation",
      "metricValueBefore" : "0",
      "impactOfAdaptation" : "unchanged after adaptation",
      "metricValueAfter" : "0"
    }, {
      "metricType" : "pass rate",
      "metricDesc" : "a comparison of \"pass rate\" before and after adaptation",
      "metricValueBefore" : "1.0",
      "impactOfAdaptation" : "unchanged after adaptation",
      "metricValueAfter" : "1.0"
    }, {
      "metricType" : "fail rate",
      "metricDesc" : "a comparison of \"fail rate\" before and after adaptation",
      "metricValueBefore" : "0.0",
      "impactOfAdaptation" : "unchanged after adaptation",
      "metricValueAfter" : "0.0"
    }, {
      "metricType" : "score",
      "metricDesc" : "a comparison of \"score\" before and after adaptation",
      "metricValueBefore" : "1.0",
      "impactOfAdaptation" : "unchanged after adaptation",
      "metricValueAfter" : "1.0"
    }, {
      "metricType" : "overall elapsed time (millis)",
      "metricDesc" : "a comparison of \"overall elapsed time (millis)\" before and after adaptation",
      "metricValueBefore" : "4415",
      "impactOfAdaptation" : "increased by 34.00% after adaptation",
      "metricValueAfter" : "5916"
    } ]
  }, {
    "categoryDesc" : "# triples",
    "metricsForCategory" : [ {
      "metricType" : "# triples provided",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "278045"
    }, {
      "metricType" : "# triples derived after analysis",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "513567"
    }, {
      "metricType" : "# triples derived after mining",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "480892"
    }, {
      "metricType" : "# triples derived after constraint analysis",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "184638"
    }, {
      "metricType" : "# triples derived through inference rule execution",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "125330"
    }, {
      "metricType" : "# triples derived elsewhere during eval workflow",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "383199"
    }, {
      "metricType" : "# triples in adaptation graph (should equal sum of above)",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "193519"
    } ]
  }, {
    "categoryDesc" : "timings",
    "metricsForCategory" : [ {
      "metricType" : "# millis required to perform bytecode task",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "1043270"
    }, {
      "metricType" : "# millis required to perform mine task",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "750196"
    }, {
      "metricType" : "# millis required to perform ingest task",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "9485"
    }, {
      "metricType" : "# millis required to perform adapt task",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "437779"
    } ]
  }, {
    "categoryDesc" : "adaptation process",
    "metricsForCategory" : [ {
      "metricType" : "# problematic dataflows with discovered constraint violations",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "158613"
    }, {
      "metricType" : "# problematic dataflows resolved",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "78967"
    } ]
  }, {
    "categoryDesc" : "code impact of adaptation",
    "metricsForCategory" : [ {
      "metricType" : "# existing classes modified",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "955724"
    }, {
      "metricType" : "# existing methods modified",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "604517"
    }, {
      "metricType" : "% of anterior code surface modified",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "0.20"
    }, {
      "metricType" : "# new classes synthesized",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "623965"
    }, {
      "metricType" : "# new methods synthesized",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "235048"
    }, {
      "metricType" : "% of posterior code surface synthesized",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "0.92"
    }, {
      "metricType" : "# new libraries added",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "129598"
    } ]
  } ]
}
```

#### An example result (evolutionary pressure)
Below is the output of a run in which the client and datasource use MDL v17 and the server uses v19.  Note that most (but not all) tests fail because the XML messages sent from client to server are not MDL v19 compliant.  Also note that in this example adaptation has no effect (this output is derived from a prototype that does not actually perform adaptation).  In a functional system, we would expect that the adapted system performs better than the original system.

```
{
  "evaluationStatus" : "COMPLETED_OK",
  "evaluationDetails" : null,
  "categories" : [ {
    "categoryDesc" : "metrics relevant to evaluation workflow",
    "metricsForCategory" : [ {
      "metricType" : "eval context ID",
      "metricDesc" : "a unique identifier for the evaluation context",
      "metricValue" : "aa6e9651-778b-41e0-a324-cb07e2e616a7"
    }, {
      "metricType" : "eval start time",
      "metricDesc" : "the starting time of the evaluation run",
      "metricValue" : "Fri Feb 08 10:53:25 EST 2019"
    }, {
      "metricType" : "eval end time",
      "metricDesc" : "the ending time of the evaluation run",
      "metricValue" : "Fri Feb 08 10:56:50 EST 2019"
    }, {
      "metricType" : "eval elapsed millis",
      "metricDesc" : "the duration of the evaluation run in milliseconds",
      "metricValue" : "204640"
    }, {
      "metricType" : "eval # cores",
      "metricDesc" : "the number of cores available to the JVM performing evaluation",
      "metricValue" : "8"
    }, {
      "metricType" : "eval max memory",
      "metricDesc" : "the memory available to the JVM performing evaluation",
      "metricValue" : "7537164288"
    } ]
  }, {
    "categoryDesc" : "impacts of adaptation",
    "metricsForCategory" : [ {
      "metricType" : "server ping (millis)",
      "metricDesc" : "a comparison of \"server ping (millis)\" before and after adaptation",
      "metricValueBefore" : "7",
      "impactOfAdaptation" : "increased by 14.29% after adaptation",
      "metricValueAfter" : "8"
    }, {
      "metricType" : "# tests performed",
      "metricDesc" : "a comparison of \"# tests performed\" before and after adaptation",
      "metricValueBefore" : "48",
      "impactOfAdaptation" : "unchanged after adaptation",
      "metricValueAfter" : "48"
    }, {
      "metricType" : "# tests that passed",
      "metricDesc" : "a comparison of \"# tests that passed\" before and after adaptation",
      "metricValueBefore" : "3",
      "impactOfAdaptation" : "unchanged after adaptation",
      "metricValueAfter" : "3"
    }, {
      "metricType" : "# tests that failed",
      "metricDesc" : "a comparison of \"# tests that failed\" before and after adaptation",
      "metricValueBefore" : "45",
      "impactOfAdaptation" : "unchanged after adaptation",
      "metricValueAfter" : "45"
    }, {
      "metricType" : "pass rate",
      "metricDesc" : "a comparison of \"pass rate\" before and after adaptation",
      "metricValueBefore" : "0.0625",
      "impactOfAdaptation" : "unchanged after adaptation",
      "metricValueAfter" : "0.0625"
    }, {
      "metricType" : "fail rate",
      "metricDesc" : "a comparison of \"fail rate\" before and after adaptation",
      "metricValueBefore" : "0.9375",
      "impactOfAdaptation" : "unchanged after adaptation",
      "metricValueAfter" : "0.9375"
    }, {
      "metricType" : "score",
      "metricDesc" : "a comparison of \"score\" before and after adaptation",
      "metricValueBefore" : "0.0625",
      "impactOfAdaptation" : "unchanged after adaptation",
      "metricValueAfter" : "0.0625"
    }, {
      "metricType" : "overall elapsed time (millis)",
      "metricDesc" : "a comparison of \"overall elapsed time (millis)\" before and after adaptation",
      "metricValueBefore" : "3626",
      "impactOfAdaptation" : "decreased by 10.40% after adaptation",
      "metricValueAfter" : "3249"
    } ]
  }, {
    "categoryDesc" : "# triples",
    "metricsForCategory" : [ {
      "metricType" : "# triples provided",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "665248"
    }, {
      "metricType" : "# triples derived after analysis",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "445940"
    }, {
      "metricType" : "# triples derived after mining",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "884730"
    }, {
      "metricType" : "# triples derived after constraint analysis",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "691087"
    }, {
      "metricType" : "# triples derived through inference rule execution",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "1048234"
    }, {
      "metricType" : "# triples derived elsewhere during eval workflow",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "182530"
    }, {
      "metricType" : "# triples in adaptation graph (should equal sum of above)",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "423859"
    } ]
  }, {
    "categoryDesc" : "timings",
    "metricsForCategory" : [ {
      "metricType" : "# millis required to perform bytecode task",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "869193"
    }, {
      "metricType" : "# millis required to perform mine task",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "583951"
    }, {
      "metricType" : "# millis required to perform ingest task",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "417710"
    }, {
      "metricType" : "# millis required to perform adapt task",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "546239"
    } ]
  }, {
    "categoryDesc" : "adaptation process",
    "metricsForCategory" : [ {
      "metricType" : "# problematic dataflows with discovered constraint violations",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "55022"
    }, {
      "metricType" : "# problematic dataflows resolved",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "259916"
    } ]
  }, {
    "categoryDesc" : "code impact of adaptation",
    "metricsForCategory" : [ {
      "metricType" : "# existing classes modified",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "428147"
    }, {
      "metricType" : "# existing methods modified",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "2679"
    }, {
      "metricType" : "% of anterior code surface modified",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "0.85"
    }, {
      "metricType" : "# new classes synthesized",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "1033998"
    }, {
      "metricType" : "# new methods synthesized",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "569826"
    }, {
      "metricType" : "% of posterior code surface synthesized",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "0.07"
    }, {
      "metricType" : "# new libraries added",
      "metricDesc" : "*** this is currently a mocked metric ***",
      "metricValue" : "843012"
    } ]
  } ]
}
```





