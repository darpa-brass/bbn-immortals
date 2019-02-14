package com.securboration.immortals.test.example;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

import org.apache.commons.io.IOUtils;

/**
 * Example of a client that pushes an arbitrarily large zipped turtle archive to
 * the knowledge repo server without relying upon any third party libraries
 * 
 * @author jstaples
 *
 */
public class TestClient {
    
    public static void main(String[] args) throws IOException{
        
        KnowledgeRepoClient client = 
                new KnowledgeRepoClient("http://localhost:8088");
        
        final String basePath = 
                "C:/Users/Securboration/Desktop/code/immortals/trunk/" +
                "knowledge-repo/examples/mini-atak-gradle/"
                ;//a possibly recursive zip archive containing .ttl files
        
        /*
         * Example 1: push a ttlz archive to the server indirectly by providing
         * a file path
         */
        {//tiny by path
            final String graphName = client.upload(
                basePath+"krgp-tiny.zip", 
                "test1"
                );
            
            System.out.println(graphName);
        }//tiny by path
        
        /**
         * Example 2: push a ttlz archive to the server directly by transmitting
         * data acros sthe wire
         */
        {//tiny
            final String graphName = client.upload(
                new File(basePath+"krgp-tiny.zip"), 
                "test1"
                );
            
            System.out.println(graphName);
        }//tiny
//        
//        {//small
//            final String graphName = client.upload(
//                new File(basePath+"krgp-small.zip"), 
//                "test1"
//                );
//            
//            System.out.println(graphName);
//        }//small
//        
//        {//large
//            final String graphName = client.upload(
//                new File(basePath+"krgp-large.zip"), 
//                "test1"
//                );
//            
//            System.out.println(graphName);
//        }//large
    }
    
    private static class KnowledgeRepoClient{
        
        private final String urlBase;
        
        private static final String charset = "UTF-8";
        
        public KnowledgeRepoClient(String urlBase) {
            super();
            this.urlBase = urlBase + "/immortalsGraphService";
        }
        
        /**
         * Uploads a file to a <b><i>local</i></b> knowledge repo service
         * instance
         * 
         * @param path
         *            the path to a possibly recursive zip archive containing
         *            .ttl files
         * @param requestedGraphName
         *            the unique name of a graph to insert the uploaded data
         *            into. If a graph with the name exists, it will be
         *            overwritten. Iff null, a randomly generated graph name
         *            will be used
         * @return the name of the graph into which triples were injected
         * @throws IOException
         *             if something goes awry
         */
        public String upload(
                final String path,
                final String requestedGraphName
                ) throws IOException{
            
            System.out.printf("uploading from path %s\n", path);
            
            final String url = requestedGraphName == null ?
                            urlBase + "/fsGraph"
                            :
                            urlBase + "/fsGraph/" + requestedGraphName
                            ;
            
            HttpURLConnection connection = 
                    (HttpURLConnection)new URL(url).openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            
            try (
                OutputStream output = connection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
            ) {
                output.write(path.getBytes());
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
                        
                        throw new IOException(new String(err.toByteArray(),charset));
                    }
                }
                
                throw e;
            }
            
            ByteArrayOutputStream readFromServer = new ByteArrayOutputStream();
            try(InputStream input = connection.getInputStream();){
                IOUtils.copy(input, readFromServer);
            }
            
            return new String(readFromServer.toByteArray(),charset);
        }
        
        /**
         * Uploads a file to a possibly remote knowledge repo service instance
         * 
         * @param file
         *            a possibly recursive zip archive containing .ttl files
         * @param requestedGraphName
         *            the unique name of a graph to insert the uploaded data
         *            into. If a graph with the name exists, it will be
         *            overwritten. Iff null, a randomly generated graph name
         *            will be used
         * @return the name of the graph into which triples were injected
         * @throws IOException
         *             if something goes awry
         */
        public String upload(
                final File file,
                final String requestedGraphName
                ) throws IOException{
            
            System.out.printf("uploading a %dB file\n", file.length());//TODO
            
            final String url = requestedGraphName == null ?
                            urlBase + "/graph"
                            :
                            urlBase + "/graph/" + requestedGraphName
                            ;
            
            HttpURLConnection connection = 
                    (HttpURLConnection)new URL(url).openConnection();
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            
            try (
                OutputStream output = connection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
            ) {
                Files.copy(file.toPath(), output);
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
                        
                        throw new IOException(new String(err.toByteArray(),charset));
                    }
                }
                
                throw e;
            }
            
            ByteArrayOutputStream readFromServer = new ByteArrayOutputStream();
            try(InputStream input = connection.getInputStream();){
                IOUtils.copy(input, readFromServer);
            }
            
            return new String(readFromServer.toByteArray(),charset);
        }
        
    }
    
    
//    private static byte[] readImage(){
//        return null;
//    }
//    
//    public static void f(){
//        //
//    }
//    
//    private static void g(){
//        
//    }
//    
//    private static void transmitToClient(){
//        
//    }

}
