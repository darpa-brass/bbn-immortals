package com.securboration.immortals.test.example;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;

import com.securboration.immortals.repo.ontology.FusekiClient;

public class TestNewService {
    
    public static void main(String[] args) throws IOException{
        SimpleKnowledgeRepoClient client = 
                new SimpleKnowledgeRepoClient("http://localhost:8088");
        
        final String graphName = client.ingest(
            new File("../../vocabulary/ontology-generate/target/classes/ontology")
            );
        
        FusekiClient fuseki = new FusekiClient("http://localhost:3030/ds");
        
        System.out.printf("graph name = %s\n", graphName);
        System.out.println(fuseki.getGraphNames());
        
        System.out.printf(
            "generated %d triples\n",
            fuseki.getModel(graphName).size()
            );
    }
    
    
    private static class SimpleKnowledgeRepoClient{
        
        private final String urlBase;
        
        private SimpleKnowledgeRepoClient(String urlBase){
            this.urlBase = urlBase;
        }
        
        private String ingest(
                final File dir
                ) throws IOException{
            
            final String url = urlBase + "/krs/ingest";
            
            HttpURLConnection connection = 
                    (HttpURLConnection)new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/text");
            
            try (
                OutputStream output = connection.getOutputStream();
            ) {
                output.write(dir.getAbsolutePath().getBytes("UTF-8"));
                output.flush();
            }
    
            try{
                final int responseCode = connection.getResponseCode();
                
                if(responseCode != 200){
                    throw new RuntimeException(
                        "response was " + responseCode + 
                        " but expected 200, with message " + connection.getResponseMessage()
                        );
                }
            } catch(IOException e){
                ByteArrayOutputStream err = new ByteArrayOutputStream();
                try(InputStream errStream = connection.getErrorStream()){
                    if(errStream != null){
                        IOUtils.copy(errStream, err);
                        
                        throw new IOException(new String(err.toByteArray()));
                    }
                }
                
                throw e;
            }
            
            ByteArrayOutputStream readFromServer = new ByteArrayOutputStream();
            try(InputStream input = connection.getInputStream();){
                IOUtils.copy(input, readFromServer);
            }
            
            return new String(readFromServer.toByteArray());
        }
    }

}
