package com.securboration.immortals.swri.eval;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Performs a query 
 * 
 * @author jstaples
 *
 */
public class EndpointStatusCheck {
    
    private final String httpVerb;
    private final String endpointUrl;
    
    private final String encoding = "UTF-8";
    
    private int responseTimeoutMillis = 10000;
    
    private int connectTimeoutMillis = 5000;
    
    public EndpointStatusCheck(
            final String httpVerb,
            final String endpointUrl
            ){
        this.endpointUrl = endpointUrl;
        this.httpVerb = httpVerb;
    }
    
    /**
     * 
     * @param maxMillis
     * @return
     */
    public void waitForEndpoint(
            final long maxMillis
            ){
        final long start = System.currentTimeMillis();
        
        while(true){
            try{
                final byte[] rawResponse = httpRequest(
                    httpVerb,
                    endpointUrl,
                    null
                    );
                
                return;
            } catch(IOException e){
                //do nothing
            }
            
            try{
                Thread.sleep(1000L);
            } catch(InterruptedException e){
                throw new RuntimeException(e);
            }
            
            if(System.currentTimeMillis() - start > maxMillis){
                throw new RuntimeException(
                    "unable to reach " + httpVerb + " " + endpointUrl + 
                    " within " + maxMillis + " ms"
                    );
            }
        }
    }
    
    private static void copy(
            InputStream input, 
            OutputStream output
            ) throws IOException{
        boolean stop = false;
        final byte[] buffer = new byte[4096];
        while(!stop){
            final int numRead = input.read(buffer);
            
            if(numRead < 0){
                stop = true;
            } else {
                output.write(buffer, 0, numRead);
            }
        }
    }
    
    private byte[] httpRequest(
            final String httpRequestMethod,
            final String httpRequestUrl,
            final byte[] httpRequestData
            ) throws IOException{
        final HttpURLConnection connection = 
                (HttpURLConnection)new URL(httpRequestUrl).openConnection();
        
        final String desc = 
                "HTTP " + httpRequestMethod + " @ " + httpRequestUrl + " with payload size " + (httpRequestData == null ? -1 : httpRequestData.length);
        
        {//configure the connection
            //set method
            connection.setRequestMethod(httpRequestMethod);
            
            //full-duplex communication
            connection.setDoInput(true);
            connection.setDoOutput(true);
            
            //set timeouts
            connection.setReadTimeout(responseTimeoutMillis);
            connection.setConnectTimeout(connectTimeoutMillis);
        }
        
        if(httpRequestData != null){//write data to the server via the connection
            connection.setRequestProperty(
                "Content-Type", 
                "application/json"
                );
            
            try (OutputStream output = connection.getOutputStream()) {
                output.write(httpRequestData);
                output.flush();
            }
        }

        {//get the response from the server
            try{
                final int responseCode = connection.getResponseCode();
                
                if(responseCode != 200){
                    throw new RuntimeException(
                        "for " + desc +
                        " response was " + responseCode + 
                        " but expected 200, with message \"" + connection.getResponseMessage() + "\""
                        );
                }
            } catch(IOException e){
                ByteArrayOutputStream err = new ByteArrayOutputStream();
                try(InputStream errStream = connection.getErrorStream()){
                    if(errStream != null){
                        copy(errStream, err);
                        
                        throw new IOException(
                            "received server-side error message for " + desc + ": " + new String(err.toByteArray(),encoding));
                    }
                }
                
                throw e;
            }
        }
        
        final ByteArrayOutputStream readFromServer = new ByteArrayOutputStream();
        
        {//read any data returned from the server
            try(InputStream input = connection.getInputStream();){
                copy(input,readFromServer);
            }
        }
        
        return readFromServer.toByteArray();
    }
    
    
    
    
    

}
