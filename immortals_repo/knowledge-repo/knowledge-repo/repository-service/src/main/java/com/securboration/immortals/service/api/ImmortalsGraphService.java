package com.securboration.immortals.service.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.securboration.immortals.repo.api.RepositoryUnsafe;
import com.securboration.immortals.service.config.ImmortalsServiceProperties;
import com.securboration.immortals.service.jar.TtlzTraverser;

/**
 * An API for the IMMoRTALS graph service
 * 
 * @author jstaples
 *
 */
@RestController
@RequestMapping("/immortalsGraphService")
public class ImmortalsGraphService {
    
    private static final Logger logger = 
            LoggerFactory.getLogger(ImmortalsGraphService.class);
    
    @Autowired(required = true)
    private ImmortalsServiceProperties properties;
    
    @Autowired(required = true)
    private RepositoryUnsafe repository;
    
    /**
     * HTTP PUT a ttlz archive into a graph with the indicated name
     * 
     * @param graphName
     *            the unique name of a graph to populate. If a graph with the
     *            provided name already exists, it will be overwritten
     * @param request
     *            used to read the graph data from the HTTP connection stream.
     *            The graph data must take the form of a ttlz archive, which are
     *            one or more TTL files, directories containing such files, or
     *            other ttlz archives that have been zip compressed.
     * @return the name of the graph into which triples have been injected. This
     *         should be the idenetical tot he requested graph name
     * @throws IOException
     *             if something goes awry
     */
    @RequestMapping(
            method = RequestMethod.PUT,
            value="/graph/{graphId}",
            consumes=MediaType.ALL_VALUE,
            produces=MediaType.TEXT_PLAIN_VALUE
            )
    public String createGraph(
            @PathVariable(value="graphId")
            String graphName,
            
            final HttpServletRequest request
            ) throws IOException{
        logger.info(request.getRequestURI());
        
        process(
            graphName,
            true,
            request.getInputStream()
            );
        
        return graphName;
    }
    
    /**
     * HTTP PUT a ttlz archive into a graph with a randomly generated name
     * 
     * @param request
     *            used to read the graph data from the HTTP connection stream.
     *            The graph data must take the form of a ttlz archive, which are
     *            one or more TTL files, directories containing such files, or
     *            other ttlz archives that have been zip compressed.
     * @return the name of the graph into which triples have been injected. This
     *         will be a randomly generated value
     * @throws IOException
     *             if something goes awry
     */
    @RequestMapping(
        method = RequestMethod.PUT,
        value="/graph",
        consumes=MediaType.ALL_VALUE,
        produces=MediaType.TEXT_PLAIN_VALUE
        )
    public String createGraph(
            @RequestBody
            String base64EncodedData
            ) throws IOException{
        final String graphName = UUID.randomUUID().toString();
        
        process(
            graphName,
            true,
            new ByteArrayInputStream(base64EncodedData.getBytes())
            );
        
        return graphName;
    }
    
    
    /**
     * HTTP PUT a ttlz archive into a graph with a randomly generated name
     * 
     * @param graphName
     *            the unique name of a graph to populate. If a graph with the
     *            provided name already exists, it will be overwritten
     * @param fsPath
     *            A path to graph data <b><i>reachable from the machine on which
     *            this service runs</i></b>. The graph data must take the form
     *            of a ttlz archive, which are one or more TTL files,
     *            directories containing such files, or other ttlz archives that
     *            have been zip compressed.
     * @return the name of the graph into which triples have been injected. This
     *         will be a randomly generated value
     * @throws IOException
     *             if something goes awry
     */
    @RequestMapping(
        method = RequestMethod.PUT,
        value="/fsGraph/{graphId}",
        consumes=MediaType.ALL_VALUE,
        produces=MediaType.TEXT_PLAIN_VALUE
        )
    public String createGraph(
            @PathVariable(value="graphId")
            String graphName,
            
            @RequestBody
            String fsPath
            ) throws IOException{
        processLocal(
            graphName,
            true,
            new File(fsPath)
            );
        
        return graphName;
    }
    
    /**
     * HTTP PUT a ttlz archive into a graph with a randomly generated name
     * 
     * @param fsPath
     *            A path reachable from this machine that points to a directory
     *            or file containing graph data. The graph data must take the
     *            form of a ttlz archive, which are one or more TTL files,
     *            directories containing such files, or other ttlz archives that
     *            have been zip compressed.
     * @return the name of the graph into which triples have been injected. This
     *         will be a randomly generated value
     * @throws IOException
     *             if something goes awry
     */
    @RequestMapping(
        method = RequestMethod.PUT,
        value="/fsGraph",
        consumes=MediaType.ALL_VALUE,
        produces=MediaType.TEXT_PLAIN_VALUE
        )
    public String createGraphFs(
            @RequestBody
            String fsPath
            ) throws IOException{
        final String graphName = UUID.randomUUID().toString();
        
        processLocal(
            graphName,
            true,
            new File(fsPath)
            );
        
        return graphName;
    }
    
    private void processLocal(
            final String graphName, 
            final boolean deleteFirst, 
            final File graphData
            ) throws FileNotFoundException, IOException{
        if(!graphData.exists()){
            throw new IOException(
                "the path provided (" + graphData.toString() + 
                ") does not exist or is not reachable on this machine"
                );
        }
            
        
        if(deleteFirst){
            //delete the graph if it exists
            repository.deleteGraph(graphName);
        }
        
        List<File> files = new ArrayList<>();
        
        if(graphData.isDirectory()){
            files.add(graphData);
        } else if(graphData.isDirectory()){
            files.addAll(FileUtils.listFiles(graphData, null, true));
        }
        
        ChunkedModelTransmitter modelTransmitter = 
                new ChunkedModelTransmitter(
                    m->repository.appendToGraph(m, graphName)
                    );
        
        AtomicLong bytesProcessed = new AtomicLong(0l);
        AtomicLong triplesProcessed = new AtomicLong(0l);
        AtomicInteger ttlFilesDiscovered = new AtomicInteger(0);
        
        final long start = System.currentTimeMillis();
        
        for(File f:files){
            try(FileInputStream fis = new FileInputStream(f)){
                TtlzTraverser.traverse(
                    fis, 
                    (name,value)->{
                        Model model = ModelFactory.createDefaultModel();
                        
                        model.read(
                            new ByteArrayInputStream(value), 
                            null, 
                            "TURTLE"
                            );
                        
                        logger.info(String.format(
                            "found ttl %s (%dB)\n", 
                            name, 
                            value.length
                            ));
                        
                        modelTransmitter.append(model);
                        
                        bytesProcessed.getAndAdd(value.length);
                        triplesProcessed.getAndAdd(model.size());
                        ttlFilesDiscovered.getAndIncrement();
                    }); 
            }
        }
        
        modelTransmitter.done();
        
        logger.info(String.format(
            "finished reading %d ttl files (%dB) from ttlz archive in %dms\n",
            ttlFilesDiscovered.get(),
            bytesProcessed.get(),
            System.currentTimeMillis() - start
            ));
    }
    
    private void process(
            final String graphName,
            final boolean deleteFirst,
            InputStream graphData
            ) throws IOException{
        
        if(deleteFirst){
            //delete the graph if it exists
            repository.deleteGraph(graphName);
        }
        
        AtomicLong bytesProcessed = new AtomicLong(0l);
        AtomicLong triplesProcessed = new AtomicLong(0l);
        AtomicInteger ttlFilesDiscovered = new AtomicInteger(0);
        
        final long start = System.currentTimeMillis();
        
//        TtlzTraverser.traverse(
//            base64EncodedData, 
//            (name,value)->{
//                Model model = ModelFactory.createDefaultModel();
//                
//                model.read(
//                    new ByteArrayInputStream(value), 
//                    null, 
//                    "TURTLE"
//                    );
//                
//                System.out.printf(
//                    "found ttl %s (%dB)\n", 
//                    name, 
//                    value.length
//                    );
//                
//                repository.appendToGraph(model, graphName);
//                
//                bytesProcessed.getAndAdd(value.length);
//                triplesProcessed.getAndAdd(model.size());
//                ttlFilesDiscovered.getAndIncrement();
//            });
        
        {
            ChunkedModelTransmitter modelTransmitter = 
                    new ChunkedModelTransmitter(
                        m->repository.appendToGraph(m, graphName)
                        );
            
            TtlzTraverser.traverse(
                graphData, 
                (name,value)->{
                    Model model = ModelFactory.createDefaultModel();
                    
                    model.read(
                        new ByteArrayInputStream(value), 
                        null, 
                        "TURTLE"
                        );
                    
                    logger.info(String.format(
                        "found ttl %s (%dB)\n", 
                        name, 
                        value.length
                        ));
                    
                    modelTransmitter.append(model);
                    
                    bytesProcessed.getAndAdd(value.length);
                    triplesProcessed.getAndAdd(model.size());
                    ttlFilesDiscovered.getAndIncrement();
                });
            
            modelTransmitter.done();
        }
        
        logger.info(String.format(
            "finished reading %d ttl files (%dB) from ttlz archive in %dms\n",
            ttlFilesDiscovered.get(),
            bytesProcessed.get(),
            System.currentTimeMillis() - start
            ));
    }
    
    /**
     * Reduces the number of fuseki transactions using a rolling buffering
     * strategy
     * 
     * @author jstaples
     *
     */
    private static class ChunkedModelTransmitter{
        private static final int MAX_SIZE = 500000;
        
        private final Object lock = new Object();
        private final Model model = ModelFactory.createDefaultModel();
        
        private final Consumer<Model> push;
        
        public void append(Model m){
            synchronized(lock){
                model.add(m);
                
                if(model.size() > MAX_SIZE){
                    logger.info("transmitting model chunk to Fuseki");
                    push.accept(model);
                    model.removeAll();
                }
            }
        }
        
        public void done(){
            synchronized(lock){
                if(model.size() > 0){
                    push.accept(model);
                    model.removeAll();
                }
            }
        }

        public ChunkedModelTransmitter(Consumer<Model> push) {
            super();
            this.push = push;
        }
    }
    
}
