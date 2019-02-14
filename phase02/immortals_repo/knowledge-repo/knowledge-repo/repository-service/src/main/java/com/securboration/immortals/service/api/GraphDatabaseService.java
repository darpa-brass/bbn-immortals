package com.securboration.immortals.service.api;

import com.securboration.immortals.repo.model.build.JarIngestor;
import com.securboration.immortals.repo.ontology.FusekiClient;
import com.securboration.immortals.service.api.DatabaseObjects.ContextRepository;
import com.securboration.immortals.service.api.DatabaseObjects.Graph;
import com.securboration.immortals.service.api.DatabaseObjects.GraphRepository;
import com.securboration.immortals.service.api.DatabaseObjects.ImmortalsContext;

import com.securboration.immortals.service.config.ImmortalsServiceProperties;
import jersey.repackaged.com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.*;

/**
 * Created by CharlesEndicott on 7/13/2017.
 */
@RestController
@EnableJpaRepositories(basePackages = {"com.securboration.immortals.service.api.DatabaseObjects"})
@EntityScan(basePackages= {"com.securboration.immortals.service.api.DatabaseObjects"})
@ComponentScan({"com.securboration.immortals.service"})
@RequestMapping("/graphService/")
public class GraphDatabaseService {

    private static final Logger logger =
            LoggerFactory.getLogger(GraphDatabaseService.class);
    
    @Autowired
    public  GraphRepository graphRepository;
    @Autowired
    public  ContextRepository contextRepository;
    @Autowired(required = true)
    private ImmortalsServiceProperties properties;

    /**
     * Persist a graph and its description in the database
     *
     * @param ttl
     *            required TTL serialization of an RDF model
     * @param type
     *            required meta-data describing the type of model being added.
     *            For now, a simple human-readable String.
     * @return randomly-generated graph name
     */
    @RequestMapping(
            method = RequestMethod.POST,
            value = "/graph",
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public @ResponseBody String graphCreate(
            @RequestBody String ttl,
            @RequestParam("type") String type
    ) {
        Graph g = new Graph("graph-" + UUID.randomUUID());

        g.setBody(ttl);
        g.setType(type);
        g.setContext("unassigned");

        graphRepository.save(g);

        return String.valueOf(g.getName());
    }
    
    /**
     * Returns the raw TTL for a requested graph
     *
     * @param graphId
     *            required ID of a graph to retrieve
     * @return the TTL form of the graph with the provided ID
     */
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/graph/{graphId}",
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public @ResponseBody String graphRetrieve(
            @PathVariable("graphId") String graphId) {
        
        Graph g = graphRepository.findOne(graphId);
        
        if (g == null) {
            throw new NoEntryPresentException(graphId);
        }
        
        return g.getBody();
    }

    /**
     * Returns a list of graphs that are of a specified type
     *
     * @param contextId
     *            optional name of context that the parsed graphs belong to
     * @param type
     *            required meta-data that describes the type of graph(s) that
     *            will be returned
     * @return an array of graph names that are of the specified type
     */
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/graph",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody List<String> graphRetrieveByType(
            @RequestParam("contextId")
                    Optional<String> contextId,

            @RequestParam("type")
                    String type
    ) {
        List<String> graphsOfType = new ArrayList<>();
        List<Graph> graphs = Lists.newArrayList(graphRepository.findAll());
        
        if (contextId.isPresent()) {
            for (Graph graph : graphs) {
                if (graph.getContext().equals(contextId.get()) && graph.getType().equals(type)) {
                    graphsOfType.add(graph.getName());
                }
            }
            
        } else {
            for (Graph graph : graphs) {
                if (graph.getType().equals(type)) {
                    graphsOfType.add(graph.getName());
                }
            }
        }
        return graphsOfType;
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/context",
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public @ResponseBody String contextCreate(
            @RequestParam("name")
                    Optional<String> name,

            @RequestBody String desc
    ) {

        String contextName = "";

        if (name.isPresent()) {
            contextName = name.get();
            if (contextRepository.findOne(contextName) != null) {
                throw new DuplicateIdentifierException(contextName);
            }
        } else {
            contextName = "context-" + UUID.randomUUID();
        }

        ImmortalsContext context = new ImmortalsContext(contextName);
        context.setDescription(desc);
        contextRepository.save(context);

        return contextName;
    }
    
    /**
     * Delete a context
     *
     * @param contextID
     *            optional ID of a context to delete. WARNING: if none provided,
     *            all contexts will be deleted
     * @return a message indicating the result of the deletion
     */
    @RequestMapping(
            method = RequestMethod.DELETE,
            value = {"/context/{contextId}", "/context"},
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public @ResponseBody String contextDelete(
            @PathVariable("contextId")
                    Optional<String> contextID
    ) {
        if(contextID.isPresent()){
            //if the context ID is set, delete a specific context
            final String id = contextID.get();
            if (!id.equals("unassigned")) {
                try {
                    contextRepository.delete(id);
                } catch (DataIntegrityViolationException exc) {
                    throw new GraphContextReferenceException(id);
                }
            } else {
                throw new ProtectedEntryExcpetion(id);
            }
            return id;
        } else {
            List<String> exceptions = new LinkedList<>();
            int counter = 0;
            //if no context ID is set, delete all contexts
            for (ImmortalsContext context : contextRepository.findAll()) {
                if (!context.getName().equals("unassigned")) {
                    try {
                        contextRepository.delete(context);
                        counter++;
                    } catch (DataIntegrityViolationException exc) {
                        exceptions.add(context.getName());
                    }
                }
            }
            
            if (!exceptions.isEmpty()) {
                throw new GraphContextReferenceException(exceptions);
            }
            return "Total of " + counter + " contexts were deleted.";
        }
    }

    /**
     * Adds the specified graph to the specified context
     *
     * @param contextID
     *            required name of context to which a graph will be added
     * @param graphID
     *            required name of graph that will be added to a context
     * @return message indicating whether the pairing was successful
     */
    @RequestMapping(
            method = RequestMethod.PATCH,
            value = "/context/{contextId}/add/{graphId}",
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public @ResponseBody String contextUpdateAdd(
            @PathVariable("contextId")
                    String contextID,

            @PathVariable("graphId")
                    String graphID
    ) {
        
        ImmortalsContext context = contextRepository.findOne(contextID);
        Graph graph = graphRepository.findOne(graphID);
        
        if (context == null) {
            throw new NoEntryPresentException(contextID);
        } else if (graph == null) {
            throw new NoEntryPresentException(graphID);
        }
        
        graph.setContext(context.getName());
        graphRepository.save(graph);
        return "Contextual Pairing Successful";
    }

    /**
     * Removes the specified graph from the specified context
     *
     * @param contextID
     *            name of context to which a graph will be removed
     * @param graphID
     *            name of graph that will be removed from a context
     * @return message indicating whether the removal was successful
     */
    @RequestMapping(
            method = RequestMethod.PATCH,
            value = "/context/{contextId}/remove/{graphId}",
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public @ResponseBody String contextUpdateRemove(
            @PathVariable("contextId")
                    String contextID,

            @PathVariable("graphId")
                    String graphID
    ) {
        Graph g = graphRepository.findOne(graphID);
        
        if (g == null) {
            throw new NoEntryPresentException(graphID);
        }
        
        g.setContext("unassigned");
        graphRepository.save(g);
        return "Graph: " + graphID + " successfully removed from context: " + contextID;
    }
    
    /**
     * Push graphs associated with the specified context to a fuseki instance
     * @param contextID name of context that associated graphs will be pushed to fuseki
     * @return name of context graph in fuseki
     */
    @RequestMapping(method = RequestMethod.POST, value = "/pushContext",
            produces = MediaType.TEXT_PLAIN_VALUE)
    public @ResponseBody String pushContext(@RequestParam("conId") String contextID) throws IOException{

        if (!contextRepository.exists(contextID)) {
            throw new NoEntryPresentException(contextID);
        }
        
        FusekiClient client = new FusekiClient(properties.getFusekiEndpointUrl());
        
        Model m = ModelFactory.createDefaultModel();
        Map<String, byte[]> models = new LinkedHashMap<>();


        InputStream jarStream =
                this.getClass().getClassLoader().getResourceAsStream(
                        "ontology/immortals-ontologies-package-r2.0.0.jar");

        ByteArrayOutputStream jarBytes = new ByteArrayOutputStream();
        IOUtils.copy(jarStream, jarBytes);
        JarIngestor.openJarForSchema(new ByteArrayInputStream(jarBytes.toByteArray()), models);

        
        for (String modelName : models.keySet()) {
            m.read(new ByteArrayInputStream(models.get(modelName)), null, "TURTLE");
        }
        
        client.setModel(m, contextID);
        
        for(Graph graph : graphRepository.findAll()) {
            m = ModelFactory.createDefaultModel();
            if (graph.getContext().equals(contextID)) {
                
                m.read(new ByteArrayInputStream(graph.getBody().getBytes()), null, "TURTLE");
                client.addToModel(m, contextID);
            }
        }
        return contextID;
    }

    @ResponseStatus(value = HttpStatus.CONFLICT, reason = "Data integrity violation")
    public class GraphContextReferenceException extends RuntimeException {
        public GraphContextReferenceException(String contextID) {
            super("Context with ID:" + contextID + " still has graphs referencing it. Remove these and try again.");
        }
        public GraphContextReferenceException(List<String> contextIDs) {
            super(createRuntimeMessage(contextIDs));
        }
    }
    
    @ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED, reason = "Protected database entry violation")
    public class ProtectedEntryExcpetion extends RuntimeException {
        public ProtectedEntryExcpetion(String entryID) {
            super("Entry with ID:" + entryID + " is a protected entry in database and cannot be removed.");
        }
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Entry not found in database")
    public class NoEntryPresentException extends RuntimeException {
        public NoEntryPresentException(String entryID) {
            super("Entry with ID: " + entryID + " was not found in database.");
        }
    }

    @ResponseStatus(value = HttpStatus.CONFLICT, reason = "Data integrity violation")
    public class DuplicateIdentifierException extends RuntimeException {
        public DuplicateIdentifierException(String entryID) {
            super("Entry with ID: " + entryID + "already exists in database.");
        }
    }
    public RuntimeException createRuntimeMessage(List<String> contextIDs) {
        String message = "Context(s) with ID(s):\n";
        for (String contextID : contextIDs) {
            message+=(contextID + "\n");
        }
        message+=("still have graphs referencing them. Remove these and try again.");
        return new RuntimeException(message);
    }
}
