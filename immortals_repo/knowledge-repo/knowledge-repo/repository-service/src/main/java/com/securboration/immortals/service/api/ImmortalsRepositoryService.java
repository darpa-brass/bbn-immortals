package com.securboration.immortals.service.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.o2t.analysis.ObjectToTriples;
import com.securboration.immortals.o2t.ontology.OntologyHelper;
import com.securboration.immortals.ontology.cp.context.ImmortalsContext;
import com.securboration.immortals.ontology.cp.context.MetaData;
import com.securboration.immortals.repo.api.RepositoryUnsafe;
import com.securboration.immortals.repo.etc.ExceptionWrapper;
import com.securboration.immortals.repo.model.build.JarIngestor;
import com.securboration.immortals.repo.ontology.FusekiClient;
import com.securboration.immortals.service.config.ImmortalsServiceProperties;

/**
 * An API for the IMMoRTALS repository service
 * 
 * @author jstaples
 *
 */
@RestController
@RequestMapping("/immortalsRepositoryService")
public class ImmortalsRepositoryService {
    
    private static final Logger logger = 
            LoggerFactory.getLogger(ImmortalsRepositoryService.class);
    
    @Autowired(required = true)
    private ImmortalsServiceProperties properties;
    
    @Autowired(required = true)
    private RepositoryUnsafe repository;
    
    @Autowired(required = true)
    private ObjectToTriplesConfiguration o2tc;
    
    /**
     * Pushes a deployment model provided as a TTL string to the triple store
     * @param ttl a TTL serialization of a deployment model produced by GME
     * @return a URI for the graph in which the deployment model was saved
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value="/pushDeploymentModel",
            consumes=MediaType.ALL_VALUE,//TODO: should be "text/turtle",
            produces=MediaType.TEXT_PLAIN_VALUE
            )
    public String pushDeploymentModelTTL(
            @RequestBody
            String ttl
            ){
        Model model = ModelFactory.createDefaultModel();
        
        model.read(
            new ByteArrayInputStream(ttl.getBytes()), 
            null, 
            "TURTLE"
            );
        
        final String graphName = 
                Helper.getImmortalsUuid(properties);
        
        repository.pushGraph(model,graphName);
        
        return graphName;
    }
    /**
     * Pushes a deployment model provided as a TTL string to the triple store
     * @param ttl a TTL serialization of a deployment model produced by GME
     * @return a URI for the graph in which the deployment model was saved
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value="/pushProjectArtifactModel",
            consumes=MediaType.ALL_VALUE,//TODO: should be "text/turtle",
            produces=MediaType.TEXT_PLAIN_VALUE
    )
    public String pushProjectArtifactModel(
            @RequestParam("ext")
                    String ext,
            @RequestBody String ttl
    ){
        Model model = ModelFactory.createDefaultModel();

        MetaData md = new MetaData(ext);

        model.read(
                new ByteArrayInputStream(ttl.getBytes()),
                null,
                "TURTLE"
        );
        
        Model metaModel = ObjectToTriples.convert(new ObjectToTriplesConfiguration(properties.getImmortalsVersion()), md);

        model.add(metaModel);

        final String graphName =
                Helper.getImmortalsUuid(properties);

        repository.pushGraph(model,graphName);

        return graphName;
    }


    @RequestMapping(
            method = RequestMethod.POST,
            value = "/getJarArtifactModels",
            consumes = MediaType.TEXT_PLAIN_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody
    List<String> getJarArtifactModels(
            @RequestBody
                    String contextUri
    ) {

        List<String> graphsInContext = new ArrayList<>();
        List<String> jarArtifactModels = new ArrayList<>();

        repository.executeSparqlQuery(getGraphUrisInContext(contextUri),
                (QuerySolution q) ->
                        graphsInContext.add(q.getLiteral("o").getString()));

        for (String graph : graphsInContext) {

            repository.executeSparqlQuery(getJarArtifactQuery(graph),
                    (QuerySolution q) ->
                            jarArtifactModels.add(graph));
        }
        return jarArtifactModels;
    }
    
    /**
     * Inserts additional resource dependencies into a graph stored in Fuseki
     * whose URI is provided.
     * 
     * @param bootstrapGraphUri
     *            result of a previous call to {@link #bootstrap()}
     * @param ucrTtl
     *            a TTL file emitted by UCR that describes the dependencies of
     *            code units upon ecosystem resources
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value="/pushUcrAnalysisModel",
            consumes=MediaType.ALL_VALUE,//TODO: should be "text/turtle"
            produces=MediaType.TEXT_PLAIN_VALUE
            )
    public void pushUcrAnalysisTtl(
            @RequestParam("bootstrapGraphUri")
            String bootstrapGraphUri,
            
            @RequestBody
            String ucrTtl
            ){
        final Model ucrModel = ModelFactory.createDefaultModel();
        final Model newResultsModel = ModelFactory.createDefaultModel();
        
        ucrModel.read(
            new ByteArrayInputStream(ucrTtl.getBytes()), 
            null, 
            "TURTLE"
            );
        
        final String ucrName = this.getClass().getSimpleName() +
                "-" +
                UUID.randomUUID().toString();
        final String newResultsName = this.getClass().getSimpleName() +
                "-" +
                UUID.randomUUID().toString();
        
        try {
            FusekiClient client = new FusekiClient(properties.getFusekiEndpointUrl());
            client.setModel(ucrModel, ucrName);
            client.setModel(newResultsModel, newResultsName);

            String constructQuery = getDataLinkConstructQuery(ucrName, bootstrapGraphUri);
            logger.warn("Executing query : " + constructQuery);
            Model queryResult = client.executeConstructQuery(constructQuery);
            if(queryResult == null){
                logger.warn("No data links found with the given input.");
                return;
            }

            String triple;
            StmtIterator statements = queryResult.listStatements();

            while (statements.hasNext()) {
                Statement s = statements.next();

                triple = String.format("<%s> <%s> <%s>\n",
                        s.getSubject(),
                        s.getPredicate(),
                        s.getObject());
                
                String updateBootstrap = "INSERT DATA" +
                        " { GRAPH <?g> { " + triple + " } }";
                updateBootstrap = updateBootstrap.replace("?g", bootstrapGraphUri);
                logger.warn("Executing query : " + updateBootstrap);
                client.executeUpdate(updateBootstrap);
                updateBootstrap = updateBootstrap.replace(bootstrapGraphUri, "http://localhost:3030/ds/data/" +
                newResultsName);
                logger.warn("Executing query : " + updateBootstrap);
                client.executeUpdate(updateBootstrap);
            }
            
        } finally {
            repository.deleteGraph(ucrName);
        }
    }
    
    
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/contextualize",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public String contextualize(
            @RequestBody 
            ContextRequest request
    ) {
        
        ImmortalsContext context = new ImmortalsContext(request.getGraphUris());
        
        Model analysisContext = ObjectToTriples.convert(new ObjectToTriplesConfiguration(
                properties.getImmortalsVersion()), context);

        final String analysisName =
                Helper.getImmortalsUuid(properties);

        repository.pushGraph(analysisContext,analysisName);

        return analysisName;
        
    }
    /**
     * Kicks off the bootstrapping process
     * @return a URI for a graph containing the bootstrapping knowledge
     * @throws IOException 
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value="/bootstrap",
            produces=MediaType.TEXT_PLAIN_VALUE
            )
    public String bootstrap() throws IOException{
        
        logger.info("bootstrapping...");
        
        final String version = properties.getImmortalsVersion();
        
        InputStream jarStream = 
                this.getClass().getClassLoader().getResourceAsStream(
                    "ontology/immortals-ontologies-package-" + version + ".jar");
        
        ByteArrayOutputStream jarBytes = new ByteArrayOutputStream();
        IOUtils.copy(jarStream, jarBytes);
        
        final String graphName = 
                Helper.getImmortalsUuid(properties);
       
        final String ns = properties.getImmortalsNs();
        
        JarIngestor.ingest(
            repository, 
            jarBytes.toByteArray(),
            ns,
            version,
            graphName, 
            ".ttl"
            );
        
        if(logger.isDebugEnabled()){
            logger.debug(
                Helper.printModel(
                    graphName,
                    repository.getGraph(graphName)));
        }
        
        return graphName;
    }
    
    /**
     * GETs a list of named graphs
     * 
     * @return an array of URIs for the graphs currently in Fuseki
     */
    @RequestMapping(
            method = RequestMethod.GET,
            value="/graphs",
            produces=MediaType.APPLICATION_JSON_VALUE
            )
    public String[] getGraphUris(){
        return repository.getGraphs().toArray(new String[]{});
    }
    
    /**
     * GETs a serialization of a graph
     * 
     * @param graphUri the URI of a graph
     * 
     * @return a Turtle serialization of the indicated graph
     */
    @RequestMapping(
            method = RequestMethod.GET,
            value="/graph",
            produces=MediaType.TEXT_PLAIN_VALUE
            )
    public String getGraph(
            @RequestParam("graphUri")
            String graphUri
            ){
        return Helper.printModel(
                graphUri,
                repository.getGraph(graphUri));
    }
    
    /**
     * Deletes a named graph.  If an empty graph URI is provided, this will 
     * delete the default graph.
     * 
     * @param graphUri a URI for a graph to delete
     * @return the URI of the deleted graph
     */
    @RequestMapping(
            method = RequestMethod.DELETE,
            value="/graph",
            produces=MediaType.TEXT_PLAIN_VALUE,
            consumes=MediaType.ALL_VALUE
            )
    public String deleteGraph(
            @RequestParam("graphUri")
            String graphUri
            ){
        
        if("".equals(graphUri)){
            graphUri = null;
        } else if(!repository.getGraphs().contains(graphUri)){
            return "graph " + graphUri + " does not exist!";
        }
        
        repository.deleteGraph(graphUri);
        
        return "deleted " + graphUri;
    }
    
    /**
     * Zeroizes Fuseki.  I.e., deletes all named graphs and the default graph.
     * Use with care.
     * 
     * @return "success" if fuseki was zeroized successfully
     */
    @RequestMapping(
            method = RequestMethod.DELETE,
            value="/zeroizeFuseki",
            produces=MediaType.TEXT_PLAIN_VALUE
            )
    public String zeroizeFuseki(){
        
        //delete all named datasets
        repository.deleteDatasets();
        
        //delete the default graph
        deleteGraph(null);
        
        return "success";
    }
    
    
    
    private static class Helper{
        
        private static String getImmortalsUuid(ImmortalsServiceProperties p){
            return UUID.randomUUID().toString() + 
                    "-IMMoRTALS-" + 
                    p.getImmortalsVersion();
        }
    
        private static String printModel(String graphName,Model model){
            
            StringBuilder sb = new StringBuilder();
            ExceptionWrapper.wrap(()->{
                sb.append(String.format("dump of graph [%s]\n", graphName));
                sb.append(String.format("----------------------------------\n"));
                sb.append(String.format(OntologyHelper.serializeModel(model, "Turtle",false)));
                sb.append(String.format("----------------------------------\n"));
            });
            
            return sb.toString();
        }
        
    }


    private String getDataLinkConstructQuery(
            final String ucrGraph,
            final String secGraph
    ){
        return (
                "" +
                        "PREFIX IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#>\r\n" +
                        "CONSTRUCT { ?dfuInstance <http://darpa.mil/immortals/ontology/r2.0.0#hasResourceDependencies> ?resourceType } WHERE {\r\n" +
                        "    GRAPH <?SEC?> { \r\n" +
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
    
    private String getGraphUrisInContext(final String contextName) {
        
        return (
                "prefix IMMoRTALS: <http://darpa.mil/immortals/ontology/r2.0.0#> \r\n" +
                        "\r\n" +
                        "select ?o \r\n" +
                        "\r\n" +
                        "where {\r\n" +
                        "  \r\n" +
                        "  graph <?g> {\r\n" +
                        "   \r\n" +
                        "  \t\t\t?s IMMoRTALS:hasGraphs ?o .\r\n" +
                        "  }\n" +
                        "        \r\n" +
                        "}"
                ).replace("?g", contextName);
    }
    
    private String getJarArtifactQuery(final String graphName) {
        
        return  (
                "prefix IMMoRTALS_bytecode: <http://darpa.mil/immortals/ontology/" + properties.getImmortalsVersion() +"/bytecode#>\r\n" +
                        "select ?s ?p\r\n" +
                        "where {\r\n" +
                        "    graph<http://localhost:3030/ds/data/?g> {\r\n" +
                        "        ?s ?p IMMoRTALS_bytecode:JarArtifact .\r\n" +
                        "    }\r\n" +
                        "}\r\n"
                ).replace("?g", graphName);
    }
    
}
