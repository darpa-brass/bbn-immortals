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
public class EndpointHelper {
    private static final String encoding = "UTF-8";
    
    private static final int responseTimeoutMillis = 10000;
    
    private static final int connectTimeoutMillis = 5000;
    
    /**
     * 
     * @param maxMillis
     * @return
     */
    public void waitFor200(
            final String httpVerb,
            final String endpointUrl,
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
            } catch(Exception e){
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
    
    public byte[] httpRequest(
            final String httpRequestMethod,
            final String httpRequestUrl,
            final byte[] httpRequestData,
            final String...headerKvs
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
                );//default is application/json

            //set headers (note: may override default json content-type)
            for(int i=0;i<headerKvs.length;i+=2){
                connection.setRequestProperty(headerKvs[i], headerKvs[i+1]);
            }
            
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
