package com.securboration.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.inetprogram.projects.mdl.MDLRootType;

import com.securboration.mls.wsdl.IngestMessageRequest;
import com.securboration.mls.wsdl.IngestMessageResponse;
import com.securboration.mls.wsdl.PingRequest;
import com.securboration.mls.wsdl.PingResponse;

/**
 * Example of a client to the IMMoRTALS CP3.1 service. Includes a main method
 * illustrating use of the client.
 * 
 * @author jstaples
 *
 */
public class MessageListenerClient {
    
	private final JAXBContext jaxContext;
	
    private final String serverUrl;
    
    private final String encoding = "UTF-8";
    
    private int responseTimeoutMillis = 5000;
    
    private int connectTimeoutMillis = 5000;
    
    private final AtomicLong messageCounter = new AtomicLong(0L);//TODO: dump messages each time
    
    private final File messageDir = new File("./messages/" + System.currentTimeMillis());
    
    private static final String soapTemplate = 
    		"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wsdl=\"http://mls.securboration.com/wsdl\">\r\n" + 
    		"   <soapenv:Header/>\r\n" + 
    		"   <soapenv:Body>\r\n" + 
    		"      ${FRAGMENT}\r\n" + 
    		"   </soapenv:Body>\r\n" + 
    		"</soapenv:Envelope>"
    		;
    
    private static final String magicBodyStart = "<SOAP-ENV:Body>";
    private static final String magicBodyEnd = "</SOAP-ENV:Body></SOAP-ENV:Envelope>";
    
    public MessageListenerClient(
    		final String serverUrl,
    		final JAXBContext jaxbContext
    		){
        this.serverUrl = serverUrl;
        this.jaxContext = jaxbContext;
    }
    
    public PingResponse ping() throws IOException{
    	PingRequest request = new PingRequest();
    	request.setTimestamp(System.currentTimeMillis());
    	
    	try {
	        return (PingResponse)deserialize(
	        		soapRequest(serialize(request),"http://mls.securboration.com/wsdl#ping"),
	        		new PingResponse()
	        		);
    	} catch(JAXBException | XPathExpressionException | TransformerFactoryConfigurationError | TransformerException e) {
    		throw new RuntimeException(e);
    	}
    }
    
    public IngestMessageResponse ingestMessage(
    		MDLRootType mdl
    		) throws IOException{
    	IngestMessageRequest request = new IngestMessageRequest();
    	request.setMessage(mdl);
    	
    	try {
    		final String requestXml = serialize(request);
    		
    		final byte[] responseFromServer = soapRequest(requestXml,null);
    		
	        return (IngestMessageResponse)deserialize(
	        		responseFromServer,
	        		new IngestMessageResponse()
	        		);
    	} catch(JAXBException | XPathExpressionException | TransformerFactoryConfigurationError | TransformerException e) {
    		throw new RuntimeException(e);
    	}
    }
    
    @mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.BinaryData
    private String serialize(
    		Object pojoRequest
    		) throws JAXBException{
    	StringWriter writer = new StringWriter();
		Marshaller m = jaxContext.createMarshaller();
		
		m.setProperty(Marshaller.JAXB_FRAGMENT, true);
		
		m.marshal(pojoRequest, writer);
    	
    	return writer.toString();
    }
    
    @mil.darpa.immortals.annotation.dsl.ontology.resources.xml.XmlInstance
    private Object deserialize(
    		final byte[] soapResponseMessage,
    		final Object pojoTemplate
    		) throws IOException, JAXBException, XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {
    	final String soapResponse = new String(soapResponseMessage,encoding);
    	
    	final int startIndex = soapResponse.indexOf(magicBodyStart) + magicBodyStart.length();
    	final int endIndex = soapResponse.lastIndexOf(magicBodyEnd);
    	
    	final String payload;
    	
    	if(startIndex < 0 || endIndex < 0){
    		payload = new String(soapResponseMessage);
    	} else {
    		payload = soapResponse.substring(startIndex,endIndex);
    	}
    	
//    	System.out.println(payload);
    	
		Unmarshaller u = jaxContext.createUnmarshaller();
		
		return u.unmarshal(new ByteArrayInputStream(payload.getBytes(encoding)));
    }
    
    
    
//    public static void main(String[] args) throws IOException, JAXBException, XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {
//    	
//    	final MessageListenerClient client = new MessageListenerClient("http://localhost:61317/ws");
//    	
//    	{
//    		PingRequest request = new PingRequest();
//    		request.setTimestamp(1234L);
//    		
//    		System.out.println(client.serialize(client.ping(request)));
//    	}
//    	
//    	{
//    		final IngestMessageRequest request = (IngestMessageRequest) client.deserialize(
//    				mdlExample.getBytes(),
//    				new IngestMessageRequest()
//    				);
//    		
//    		client.ingestMessage(request);
//    		
//    		System.out.println(client.serialize(client.ingestMessage(request)));
//    	}
//    }
    
    
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
    
    private byte[] soapRequest(
    		final String xmlFragment,
    		final String actionUri
    		) throws IOException {//TODO: dump the request and response from this method
    	
    	final String soapMessage = soapTemplate.replace("${FRAGMENT}", xmlFragment);
    	
//    	System.out.println("*** " + soapMessage.replace("\r\n", ""));
    	
    	final byte[] xmlPayload = soapMessage.getBytes(encoding);
    	
    	final byte[] xmlResponse = httpRequest(
    			"POST",
    			serverUrl,
    			xmlPayload,
    			"Content-Type", "text/xml; charset=" + encoding,
    			"SOAPAction",actionUri != null ? actionUri : ""
    			);
    	
    	{
    		final File outputDir = new File(messageDir,"interaction-"+messageCounter.incrementAndGet());
    		
    		FileUtils.writeBytesToFile(
    				outputDir.getAbsolutePath(), 
    				"request.xml", 
    				xmlPayload
    				);
    		
    		FileUtils.writeBytesToFile(
    				outputDir.getAbsolutePath(), 
    				"response.xml", 
    				xmlResponse
    				);
    	}
    	
    	return xmlResponse;
    }
    
    private byte[] httpRequest(
            final String httpRequestMethod,
            final String httpRequestUrl,
            final byte[] httpRequestData,
            final String...requestKvs
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
            
            if(httpRequestData != null) {
            	connection.setRequestProperty(
                        "Content-Type", 
                        "application/json"
                        );
            }
        }
        
        {//set kvs
        	for(int i=0;i<requestKvs.length;i+=2) {
                connection.setRequestProperty(
                		requestKvs[i], 
                		requestKvs[i+1]
                		);
        	}
        }
        
        if(httpRequestData != null){//write data to the server via the connection
            
            
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
    

    
    
    
    
    
    
    
    
    
    
    
    //TODO
    
    
    private static final String mdlExample =
    		"		<ns4:ingestMessageRequest\r\n" + 
    		"			xmlns:ns2=\"https://wsmrc2vger.wsmr.army.mil/rcc/manuals/106-11\"\r\n" + 
    		"			xmlns:ns4=\"http://mls.securboration.com/wsdl\"\r\n" + 
    		"			xmlns:ns3=\"http://inetprogram.org/projects/MDL\">\r\n" + 
    		"			<message>\r\n" + 
    		"				<ns3:DatabaseID>example id string</ns3:DatabaseID>\r\n" + 
    		"				<ns3:Checksum>not present</ns3:Checksum>\r\n" + 
    		"				<ns3:ConfigurationVersion>0.0.1</ns3:ConfigurationVersion>\r\n" + 
    		"				<ns3:DirtyBit>false</ns3:DirtyBit>\r\n" + 
    		"				\r\n" + 
    		"			</message>\r\n" + 
    		"		</ns4:ingestMessageRequest>";
}