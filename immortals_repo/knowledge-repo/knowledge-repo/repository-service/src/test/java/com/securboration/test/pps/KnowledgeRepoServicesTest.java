package com.securboration.test.pps;



import com.securboration.immortals.ontology.cp.context.MetaData;
import com.securboration.immortals.repo.etc.WebServiceStrings;
import org.apache.commons.io.FileUtils;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by CharlesEndicott on 6/22/2017.
 */
public class KnowledgeRepoServicesTest {
    
    WebServiceStrings serviceStrings = new WebServiceStrings();

    private static final Logger logger =
            LoggerFactory.getLogger(KnowledgeRepoServicesTest.class);
    
    //@Test
    public void runContextProductionTests() throws IOException {
        logger.info("Adding test graphs ...");
        List<String> graphNames = initialize();
        assert(graphNames.size() == 4);
        logger.info("Done. Creating context ...");
        String contextID = createContext();
        assert(!contextID.equals(""));
        logger.info("Done. Adding graphs to context ...");
        boolean contextSuccess =  addGraphsToContext(contextID, graphNames);
        assert(contextSuccess);
        logger.info("Done. Parsing context for type \"jar\" ...");
        graphNames = getGraphsOfType(contextID, "jar");
        assert(graphNames.size() == 3);
        logger.info("Done. Removing one jar model from context ...");
        removeGraphFromContext(contextID, graphNames.get(0));
        graphNames = getGraphsOfType(contextID, "jar");
        assert(graphNames.size() == 2);
        logger.info("Done. Removing the rest of models from context ...");
        graphNames.addAll(getGraphsOfType(contextID, "class"));
        for (String graphName : graphNames) {
            removeGraphFromContext(contextID, graphName);
        }
        logger.info("Done. Deleting context ...");
        String deletedContextID = deleteContext(contextID);
        assert(deletedContextID.equals(contextID));
        logger.info("Done. Context production tests completed.");
    }
    
    private ArrayList<String> initialize() {
        RestTemplate restTemplate = new RestTemplate();
        ArrayList<String> graphNames = new ArrayList<>();
        byte[] encoded;
        String ext;
        
        Collection<File> collectFiles = FileUtils.listFiles(
                new File(PATH_TO_TESTDIRS),null , true);
        List<File> testFiles = new ArrayList<>(collectFiles);
        
        // Parse files in test directories for type meta-data
        for (File testFile : testFiles) {
            
            String fileName = testFile.getName();
            fileName = fileName.substring(0, fileName.lastIndexOf(".ttl"));
            
            if (fileName.endsWith("jar")) {
                ext = MetaData.OriginsOfGraph.JAR.getOrigin();
            } else if (fileName.endsWith("class")) {
                ext = MetaData.OriginsOfGraph.CLASS.getOrigin();
            } else if (fileName.endsWith("ttl")) {
                ext = MetaData.OriginsOfGraph.EMBEDDED.getOrigin();
            } else {
                ext = MetaData.OriginsOfGraph.UNSUPPORTED.getOrigin();
            }
            
            try {
                encoded = Files.readAllBytes(Paths.get(testFile.getAbsolutePath()));
            } catch (IOException exc) {
                return graphNames;
            }
            String model = new String(encoded, Charset.defaultCharset());

            // Call web service to add graph to database and keep track of their names
            String url = serviceStrings.createGraphUrl(ext);
            String graphName = restTemplate.postForObject(
                    url, model,
                    String.class
            );
            graphNames.add(graphName);
        }
        
        return graphNames;
    }
    
    private String createContext(){
        RestTemplate restTemplate = new RestTemplate();
        String contextName = "";
        String url = serviceStrings.createContextUrl(Optional.empty());
        
        // Simple. Call web service to create context and keep track of its name
        contextName = 
                restTemplate.postForObject(url, TEST_CONTEXT_DESCRIPTION, String.class);
        
        return contextName;
    }
    
    private boolean addGraphsToContext(String contextID, List<String> graphNames) {
        RestTemplate restTemplate = new RestTemplate();
        HttpClient client = HttpClients.createDefault();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(client));
        String url;
        
        // Call web service to add all of the parsed graphs to the created context
        for (String graphName : graphNames) {
            url = serviceStrings.addGraphToContextUrl(contextID, graphName);
            ResponseEntity<String> result = restTemplate.exchange(url,
                    HttpMethod.PATCH, null, String.class);
            if (!result.getBody().equals(SUCCESSFUL_PARING)) {
                return false;
            }
        }
        return true;
    }
    
    private List<String> getGraphsOfType(String contextName, String type) {
        RestTemplate restTemplate = new RestTemplate();
        String url = serviceStrings.getGraphsOfTypeUrl(type, Optional.of(contextName));
        // Call web service to search through created context graphs, and return only those of type "jar"
        List<String> graphsOfType = restTemplate.getForObject(url, ArrayList.class);
        
        return graphsOfType;
    }
    
    private void removeGraphFromContext(String contextID, String graphID) {
        RestTemplate restTemplate = new RestTemplate();
        HttpClient client = HttpClients.createDefault();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(client));
        String url = serviceStrings.removeGraphFromContextUrl(contextID, graphID);
        
        restTemplate.exchange(url, HttpMethod.PATCH, null, String.class);
    }
    
    private String deleteContext(String contextID) {
        RestTemplate restTemplate = new RestTemplate();
        HttpClient client = HttpClients.createDefault();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(client));
        String url= serviceStrings.deleteContextUrl(Optional.of(contextID));
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);
        
        return responseEntity.getBody();
    }
    
    private final String PATH_TO_TESTDIRS = "./src/test/java/com/securboration/test/pps/testDirs";
    private final String SUCCESSFUL_PARING = "Contextual Pairing Successful";
    private final String TEST_CONTEXT_DESCRIPTION = "I describe graphs that are useful in an environment where bandwidth is an unstable resource";
}
