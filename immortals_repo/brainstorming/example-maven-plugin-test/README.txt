The purpose of this module is to test that the IMMoRTALS Maven plugin works.

Test procedure:
	0) Build and install the immortals plugin project

	1) Run 
		mvn clean install > dump1.txt
	and observe the output in dump1.txt
	
	2) Run
		mvn clean install -Dimmortalize > dump2.txt
	This triggers a maven profile that results in bytecode instrumentation being performed on the original application.
	You should observe *additional* output to stdout as a result of the instrumentation.  For example:
> invoked method [com/securboration/immortals/test/Main] [main] [([Ljava/lang/String;)V]
> invoked method [com/securboration/immortals/test/Main] [<init>] [()V]
> invoked method [com/securboration/immortals/test/Main] [test] [(Lcom/securboration/immortals/fuseki/FusekiClient;)V]
> invoked method [com/securboration/immortals/test/Main] [testPushModel] [(Lcom/securboration/immortals/fuseki/FusekiClient;)Lcom/securboration/immortals/fuseki/FusekiClient;]
> invoked method [com/securboration/immortals/test/Main] [getTestModel] [()Lorg/apache/jena/rdf/model/Model;]
> invoked method [com/securboration/immortals/test/Main] [testGetModel] [(Lcom/securboration/immortals/fuseki/FusekiClient;)Lorg/apache/jena/rdf/model/Model;]