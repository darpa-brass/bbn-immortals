${IMMORTALS_REPO}/brainstorming/spiral0
	This directory contains the following modules:

	immortals-ontologies-vocab
		Contains marked up POJOs used to generate the IMMoRTALS semantic vocabulary.  Analogous
		 the the way annotated JAXB classes can be used in a code-first manner to generate a 
		 schema.

	immortals-ontologies-pojo-annotations
		Contains annotations that can be applied to POJOs in immortals-ontologies-vocab to effect 
		 a transformation into triples.

	immortals-ontologies-vocab-instances
		Contains logic for instantiating semantic concepts expressed in terms of the POJOs defined
		 in immortals-ontologies-vocab.  E.g., scanning a JAR for @DFU annotations.
		 
	immortals-repository-api
		Contains an API for interacting with Fuseki in terms of the POJOs defined in the 
		 immortals-ontologies-vocab module.  Essentially a wrapper around Jena.
		
	immortals-repository-ingestor
		Ingests the instantiated triples produced by the immortals-vocab-instances module 
		 into a running Fuseki instance.
		
	immortals-ontologies-generate
		Generates a vocabulary from the annotated POJOs in immortals-ontologies-vocab.
		Generates individuals from the instances in immortals-ontologies-vocab-instances.
		Adds these ontologies to a JAR.

	immortals-object-to-triples
		Mechanism for converting Java classes into a semantic vocabulary.
		Mechanism for converting Java objects into individuals.

	immortals-deployment-ingestor
		Mechanism for ingesting Vanderbilt's deployment models.  Currently a work in progress.
		
	immortals-maven-plugin
		Currently unused.
		
		
How to use this software:
	To build the vocabulary and instances:
		cd ${IMMORTALS_REPO}/brainstorming/spiral0
		mvn clean install
		
		The default build invokes the following main methods:
			com.securboration.immortals.instantiation.JavaToTriplesMain 
			 (immortals-ontologies-vocab-instances), which accepts the following args:
				Arg0: vocab file to generate
				Arg1: language to write vocabulary (e.g., Turtle)
				Arg2: immortals version
				Arg3: directory containing classes to generate vocabulary from
				Arg4: directory containing source code from which classes were generated
				Arg5: comma-separated list of package prefixes to ignore 
				
			com.securboration.immortals.instantiation.InstancesToTriplesMain 
			 (immortals-ontologies-vocab-instances), which accepts the following args:
				Arg0: directory in which output will be generated
				Arg1: immortals version
				Arg2: path to root of IMMoRTALS project
				Arg3: SVN repository base URL
				Arg4: immortals library version (i.e., the JAR version specified by BBN)
				Arg5...: source paths to scan (to convert local file system URLs into 
				          repo URLs)
		
		The result of a successful build is the creation of the immortals-ontologies-generate 
		 JAR, which contains triples describing vocabulary and individuals.
		
				
	To inject the bootstrapping triples into a running Fuseki instance:
		cd ${IMMORTALS_REPO}/brainstorming/spiral0/immortals-repository-ingestor
		NOTE1: this can also be done from the project root since profiles are inherited
		NOTE2: Fuseki must be running for this to work
		mvn clean install -Pbootstrap

		Note that this is essentially a wrapper around 
			com.securboration.immortals.repo.main.IngestMain, which accepts the following args:
				Arg0: immortals version
				Arg1: path to a vocabulary jar
				Arg2: language suffix for models to read from the vocabulary jar (e.g., .ttl)

		The result of a successful build is the injection of the triples produced by 
		 immortals-ontologies-generate into a running Fuseki instance.  You will also see a
		 copy of the resulting graph retrieved from Fuseki in the current directory.
				
		
		
		
		