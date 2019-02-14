package com.securboration.immortals.repo.test.linkage;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import com.securboration.immortals.repo.ontology.FusekiClient;
import com.securboration.immortals.repo.test.queries.QueryTestBase;

public class TestDataLinkageViaSparql extends QueryTestBase {
    
    public static void main(String[] args){
        LogManager.getRootLogger().setLevel(Level.WARN);//TODO
        
        /*
         * This example illustrates how data emitted by UCR can be utilized to
         * infer additional information about resource dependencies.
         * 
         * At its point of definition, annotations may optionally be used to
         * declare explicit resource dependencies for a DFU. E.g., a code unit
         * might depend upon network IO. This annotation-based process is manual
         * and therefore prone to error. UCR can augment these manual
         * annotations with a much more powerful and fully automated static
         * analysis. UCR does this by detecting API calls originating from DFU
         * logic that are indicative of such dependencies.
         * 
         * UCR's analysis knows nothing about DFUs. Rather, it knows the
         * bytecode pointers of code regions at which these calls originate. We
         * want to merge the DFU-agnostic results provided by UCR of this
         * analysis into our model that understands the mappings of those code
         * regions to DFU instances.
         * 
         * 
         * TODO1: currently, the solution CONSTRUCTs an entirely new graph. We
         * could also use INSERT to inject the emitted triples directly into an
         * existing graph. 
         * 
         * TODO2: implement this as a web service call in the 
         * knowledge-repo-service
         */
        
        new TestDataLinkageViaSparql().testDataLinkage();
    }
    
    public void testDataLinkage(){
        final CleanupContext cleanup = new CleanupContext();
        final FusekiClient client = super.acquireFusekiConnection();
        
        System.out.printf(
            "*** before test: %d graphs\n",
            client.getGraphNames().size()
            );
        try{
            runTest(client,cleanup);
        }finally{
            for(String graphToCleanup:cleanup.getGraphsToCleanup()){
                client.deleteModel(graphToCleanup);
            }
            
            System.out.printf(
                "*** after test: %d graphs\n",
                client.getGraphNames().size()
                );
        }
    }
    
    private String pushModelIntoFuseki(
            FusekiClient client, 
            CleanupContext cleanup, 
            Model modelToPush, 
            String tag
            ){
        final String name = 
                this.getClass().getSimpleName() + 
                "-" + 
                UUID.randomUUID().toString();
        
        client.setModel(modelToPush, name);
        
        cleanup.add(name, tag);
        
        return name;
    }
    
    private void runTest(FusekiClient client,CleanupContext cleanup){
        final String ucrGraph = pushModelIntoFuseki(
            client,
            cleanup,
            getModel(TestData.inputFromUcr),
            "UCR input"
            );
        
        final String secGraph = pushModelIntoFuseki(
            client,
            cleanup,
            getModel(TestData.inputFromBytecodeAnalysis),
            "Securboration input"
            );
        
        final String expectedOutGraph = pushModelIntoFuseki(
            client,
            cleanup,
            getModel(TestData.expectedOutput),
            "expected output"
            );
        
        final String actualOutGraph = pushModelIntoFuseki(
            client,
            cleanup,
            ModelFactory.createDefaultModel(),
            "actual output"
            );
        
        runTestUsingGraphs(
            client,
            ucrGraph,secGraph,actualOutGraph,expectedOutGraph
            );
    }
    
    private void runTestUsingGraphs(
            FusekiClient client,
            final String ucrGraph,
            final String secGraph,
            final String outGraph,
            final String expectedGraph
            ){
        runInferenceQuery(client,ucrGraph,secGraph,outGraph);
        
        verifyOutMatchesExpected(
            client.getModel(outGraph),
            client.getModel(expectedGraph)
            );
    }
    
    private void runInferenceQuery(
            FusekiClient client,
            final String ucrGraph,
            final String secGraph,
            final String outGraph
            ){
        final String construct = getConstructQuery(ucrGraph,secGraph);
        
        System.out.println(construct);
        
        Model m = client.executeConstructQuery(construct);
        
        client.setModel(m, outGraph);
    }
    
    //TODO: could also INSERT the discovered dependencies into secGraph
    private String getConstructQuery(
            final String ucrGraph,
            final String secGraph
            ){
        return (
                "" +
                "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" + 
                "CONSTRUCT { ?dfuInstance <http://darpa.mil/immortals/ontology/r2.0.0#hasResourceDependencies> ?resourceType } WHERE {\r\n" + 
                "    GRAPH <http://localhost:3030/ds/data/?SEC?> { \r\n" + 
                "        ?dfuInstance a <http://darpa.mil/immortals/ontology/r2.0.0/dfu/instance#DfuInstance> .\r\n" + 
                "        ?dfuInstance IMMoRTALS:hasFunctionalAspects ?functionalAspect .\r\n" + 
                "        ?functionalAspect IMMoRTALS:hasMethodPointer ?pointer .\r\n" + 
                "    } .\r\n" + 
                "    GRAPH <http://localhost:3030/ds/data/?UCR?> { \r\n" + 
                "        ?report a <http://darpa.mil/immortals/ontology/r2.0.0/analysis#AnalysisReport> .\r\n" + 
                "        ?report IMMoRTALS:hasMeasurementProfile ?measurementProfile .\r\n" + 
                "        ?measurementProfile IMMoRTALS:hasCodeUnit ?codeUnit .\r\n" + 
                "        ?codeUnit IMMoRTALS:hasPointerString ?pointer . \r\n" + 
                "        ?report IMMoRTALS:hasDiscoveredDependency ?dependencyAssertion .\r\n" + 
                "        ?dependencyAssertion IMMoRTALS:hasDependency ?resourceType .\r\n" + 
                "    } .\r\n" + 
                "}"
                ).replace("?SEC?", secGraph).replace("?UCR?", ucrGraph);
    }
    
    private void verifyOutMatchesExpected(Model out, Model expected){
        Set<String> outTriples = getTriples(out);
        Set<String> expectedTriples = getTriples(expected);
        
        Set<String> onlyInExpected = new HashSet<>(expectedTriples);
        onlyInExpected.removeAll(outTriples);
        
        System.out.printf(
            "CONSTRUCTed the following %d triples:\n",
            outTriples.size()
            );
        for(String s:outTriples){
            System.out.printf("\t%s\n", s);
        }
        
        if(onlyInExpected.size() > 0){
            System.out.printf(
                "The following %d expected triples were missing in " +
                "the emitted output:\n", 
                onlyInExpected.size()
                );
            for(String s:onlyInExpected){
                System.out.printf("\t%s\n", s);
            }
            
            throw new RuntimeException("verification failed!");
        }
    }
    
    private static Set<String> getTriples(Model m){
        Set<String> triples = new HashSet<>();
        
        if(m == null){
            return triples;
        }
        
        StmtIterator statements = m.listStatements();

        while (statements.hasNext()) {
            Statement s = statements.next();

            triples.add(
                String.format(
                    "[%s] [%s] [%s]\n", 
                    s.getSubject(),
                    s.getPredicate(), 
                    s.getObject()
                    )
                );
        }
        
        return triples;
    }
    
    private static Model getModel(String ttl){
        Model m = ModelFactory.createDefaultModel().read(
            new ByteArrayInputStream(ttl.getBytes()), 
            null, 
            "TURTLE"
            );
        
        System.out.printf(
            "read %d triples from a %dB TTL\n", 
            m.size(), 
            ttl.getBytes().length
            );
        
        return m;
    }

}
