package com.securboration.immortals.service.eos.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.securboration.immortals.service.eos.api.types.EosType;
import com.securboration.immortals.service.eos.api.types.EvaluationConfiguration;
import com.securboration.immortals.service.eos.api.types.EvaluationStatusReport;

/**
 * Example of a client to the IMMoRTALS CP3.1 service.
 * 
 * @author jstaples
 *
 */
public class EosClient {
    
    private final String serverUrl;
    
    private final String encoding = "UTF-8";
    
    private int responseTimeoutMillis = 100000;//TODO: think about making this configurable
    
    private int connectTimeoutMillis = 5000;//TODO: think about making this configurable
    
    public EosClient(final String serverUrl){
        this.serverUrl = serverUrl;
    }
    
    /**
     * 
     * @return the server's epoch timestamp
     * @throws IOException
     *             if something goes wrong
     */
    public long ping() throws IOException{
        final byte[] rawResponse = httpRequest(
            "GET",
            getUrlWithPath("ping"),
            null
            );
        
        final String stringResponse = new String(rawResponse,encoding);
        
        return Long.parseLong(stringResponse);
    }
    
    /**
     * Kicks off an asynchronous evaluation sequence with the provided
     * configuration
     * 
     * @param config
     *            the evaluation configuration
     * @return a key that uniquely identifies the kicked-off evaluation process
     *         by which its status can subsequently be queried
     * @throws IOException
     *             if something goes wrong
     */
    public String evaluate(
            final EvaluationConfiguration config
            ) throws IOException{
        final byte[] rawResponse = httpRequest(
            "POST",
            getUrlWithPath("evaluate"),
            config.toJson().getBytes(encoding)
            );
        
        return new String(rawResponse,encoding);
    };
    
    /**
     * Kicks off an asynchronous dynamic analysis sequence with the provided
     * configuration
     * 
     * @param config
     *            the configuration for the dynamic analysis run
     * @return a key that uniquely identifies the kicked-off evaluation process
     *         by which its status can subsequently be queried
     * @throws IOException
     *             if something goes wrong
     */
    public String dynamicAnalysis(
            final EvaluationConfiguration config
            ) throws IOException{
        final byte[] rawResponse = httpRequest(
            "POST",
            getUrlWithPath("dynamicAnalysis"),
            config.toJson().getBytes(encoding)
            );
        
        return new String(rawResponse,encoding);
    };
    
    public EvaluationStatusReport status(final String key) throws IOException{
        final byte[] rawResponse = httpRequest(
            "GET",
            getUrlWithPath("status") + "?contextId=" + key,
            null
            );
        
        return EosType.fromJson(
            new String(rawResponse,encoding), 
            EvaluationStatusReport.class
            );
    }
    
    private String getUrlWithPath(final String methodName){
        StringBuilder sb = new StringBuilder();
        
        sb.append(serverUrl);
        
        if(serverUrl.endsWith("/")){
            //do nothing
        } else {
            sb.append("/");
        }
        
        sb.append(methodName);
        
        return sb.toString();
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

    
    public String getServerUrl() {
        return serverUrl;
    }
    
    
    
    
    

}
