Build the project:
	mvn clean install
	
Additional optional build flags:
	-DskipTests (skips tests)
	-Pbootstrap (push IMMoRTALS ontology into a running Fuseki instance)
	-PincludeBytecodeModels (include fine-grained bytecode models in ontology)
	-PincludeAstModels (include abstract syntax tree models in ontology)
	-DvalidateOntology (runs a soundness check on the generated ontology, this may take quite some time to complete)

Multiple build flags can be used simultaneously:
	mvn clean install -DskipTests -Djunit.debug -Pbootstrap -PincludeAstModels -PincludeBytecodeModels
	
Build the project and skip all tests:
	mvn clean install -DskipTests
	
Build the project and validate all generated ontologies (using OWL + RDFS inferences):
	mvn clean install -Pbootstrap -DvalidateOntology
	
Build the project and validate all generated ontologies using some other inferencing engine:
	mvn clean install -Pbootstrap -DvalidationSpec=[one of the following]
		OWL_MEM_RDFS_INF
        OWL_MEM_MICRO_RULE_INF
        OWL_MEM_MINI_RULE_INF
		OWL_LITE_MEM_RULES_INF
		OWL_DL_MEM_RULES_INF
        OWL_MEM_RULE_INF	
	The options above are ordered by increasing complexity.  RDFS_INF is guaranteed to run 
	 in low order polynomial time whereas there are no guarantees that the OWL_MEM_RULE_INF
	 will even complete.  OWL_DL_MEM_RULES_INF will complete, but it will probably take a 
	 very long time (and large amount of memory) to do so for nontrivial ontologies.

Start the knowledge-repo service (on port 8080):
	java -jar knowledge-repo/repository-service/target/immortals-repository-service-boot.war
	
Start the service on some other port:
	java -jar knowledge-repo/repository-service/target/immortals-repository-service-boot.war -Dserver.port=9999
	
	After starting the service on port 8080, test it:
	http://localhost:8080/immortalsTestService/ping
	
	A triple navigation UI can be viewed at:
	http://localhost:8080/Query.html

	List the graphs in Fuseki:
	GET http://localhost:8080/immortalsRepositoryService/graphs
	
	Bootstrap a new knowledge graph:
	POST http://localhost:8080/immortalsRepositoryService/bootstrap
	
	Generate a flattened, stringified view of a named resource:
	GET http://localhost:8080/stringify?graphName=http://localhost:3030/ds/data/IMMoRTALS_r2.0.0&individualUri=[a uri goes here e.g. http://darpa.mil/immortals/ontology/r2.0.0/...]
	
	The ontology vocabulary can be viewed at:
	http://localhost:8080/ontology/immortals-vocab/immortals_vocabulary.ttl
	
