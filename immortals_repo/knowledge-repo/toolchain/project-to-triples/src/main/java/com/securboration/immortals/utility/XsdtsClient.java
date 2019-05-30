package com.securboration.immortals.utility;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Example of a client to the IMMoRTALS CP3.1 service. Includes a main method
 * illustrating use of the client.
 *
 * @author jstaples
 *
 */
public class XsdtsClient {

    private final String serverUrl;

    private final Charset encoding = StandardCharsets.UTF_8;

    private int responseTimeoutMillis = 5000;

    private int connectTimeoutMillis = 5000;
    
    private static AtomicInteger requestCounter = new AtomicInteger(0);

    public XsdtsClient(final String serverUrl){
        this.serverUrl = serverUrl;
    }

    public long ping() throws IOException{
        final byte[] rawResponse = httpRequest(
                "GET",
                getUrlWithPath("ping"),
                null
        );

        final String stringResponse = new String(rawResponse,encoding);

        return Long.parseLong(stringResponse);
    }

    public String getXsdTranslation(final TranslationProblemDefinition problem) throws UnsupportedEncodingException, JsonProcessingException, IOException{
        String xslt = new String(
                httpRequest(
                        "POST",
                        getUrlWithPath("translate"),
                        new ObjectMapper().writeValueAsString(problem).getBytes(encoding)
                ),
                encoding
        ).trim();
        
        String rawResponse = xslt;
        final Map<String,String> responseProperties = new HashMap<>();
        if(xslt.startsWith("{")){
            //detect a JSON response from the AQL service impl
            //(our test service returns XML)
            Map<String,Object> myMap = new HashMap<>();
    
            ObjectMapper objectMapper = new ObjectMapper();
            myMap = objectMapper.readValue(xslt, HashMap.class);
            
//            if(myMap.containsKey("error")){
//                final String error = myMap.get("error");
//                if(error.length() > 0) {
//                    throw new RuntimeException("The AQL XSD translation service returned an error: " + error);
//                }
//            }
            
            xslt = ((String)myMap.get("xslt"))
                    .trim()//fix whitespace before XML preamble
                    .replace("mdl:MDLRootType", "mdl:MDLRoot")//fix incorrect node name
                    ;
            
            if(myMap.containsKey("translationMetrics")){//get the translation metrics
                final File metricsFile = new File("xsdts-client/metrics");
                
                final String s = myMap.get("translationMetrics").toString();
                
                FileUtils.writeStringToFile(metricsFile, s + "\n", true);
            }
        }
        
        {//dump the req/response 
            final File dir = new File("xsdts-client/" + requestCounter.getAndIncrement());
            FileUtils.writeStringToFile(
                new File(dir,"request.json"), 
                new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(problem),
                StandardCharsets.UTF_8
                );
            FileUtils.writeStringToFile(
                new File(dir,"response.xslt"), 
                xslt,
                StandardCharsets.UTF_8
                );
            
            FileUtils.writeStringToFile(
                new File(dir,"response.json"), 
                rawResponse,
                StandardCharsets.UTF_8
                );
        }
        
        return xslt;
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






}
