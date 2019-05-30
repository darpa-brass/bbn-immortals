package com.securboration.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.inetprogram.projects.mdl.MDLRootType;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
    
    private String serialize(
    		Object pojoRequest
    		) throws JAXBException{
    	if(pojoRequest instanceof IngestMessageRequest) {
    		return outboundMdlShim(serializeInternal(pojoRequest));
    	}
    	
    	return serializeInternal(pojoRequest);
    }
    
    private String serializeInternal(
    		Object pojoRequest
    		) throws JAXBException{
    	StringWriter writer = new StringWriter();
		Marshaller m = jaxContext.createMarshaller();
		
		m.setProperty(Marshaller.JAXB_FRAGMENT, true);
		
		m.marshal(pojoRequest, writer);
    	
    	return writer.toString();
    }
    
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
    	
    	if(payload.contains("ingestMessageResponse")) {//TODO: janky
    		return inboundMdlShim(payload);
    	}
    	
    	//payload looks like <ns5:ingestMessageResponse> ... </ns5:ingestMessageResponse>
    	
		Unmarshaller u = jaxContext.createUnmarshaller();
		
		return u.unmarshal(new ByteArrayInputStream(payload.getBytes(encoding)));
    }
    
	/**
	 * 
	 * @param mdl an XML document containing a single MDLRoot element
	 * @return a possibly modified version of the input. An XML document containing
	 *         a single MDLRoot element.
	 */
    private String processMdlReadFromServer(
    		final String mdl
    		) {
    	return mdl;//TODO: XSLT may need to be applied here
    }
    
    /**
	 * 
	 * @param mdl an XML document containing a single MDLRoot element
	 * @return a possibly modified version of the input. An XML document containing
	 *         a single MDLRoot element.
	 */
    private String processMdlSentToServer(
    		final String mdl
    		) {
    	return mdl;//TODO: XSLT may need to be applied here
    }
    
    /**
     * Emulates application-specific XML parsing logic (DOM API)
     * 
     * @param requestElement an XML document containing a single ingestMessageResponse element
     * @return 
     */
    private String outboundMdlShim(
			final String requestElement
			) {
    	try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new ByteArrayInputStream(requestElement.getBytes(encoding)));
			
			final Element root = doc.getDocumentElement();
			
			final NodeList nodes = root.getChildNodes();
			if(nodes.getLength() == 0 || nodes.getLength() > 1) {
				throw new RuntimeException("expected 1 but got " + nodes.getLength());
			}
			
			final Node message = nodes.item(0);
			
			if(!message.getNodeName().equals("message")) {
				throw new RuntimeException("expected \"message\" but got " + message.getNodeName());
			}
			
			final Node renamedMessage = doc.renameNode(message, null, "MDLRoot");
			
				@mil.darpa.immortals.annotation.dsl.ontology.resources.xml.XmlDocument(xmlVersion="1.0", encoding="UTF-8", schemaNamespace="http://inetprogram.org/projects/MDL", schemaVersion = "17")
				final String processedMdl = processMdlSentToServer(nodeToString(renamedMessage));
				
				{
					final Document result = dbFactory.newDocumentBuilder().newDocument();
					final Element ingestMessageRequest = result.createElement("ingestMessageRequest");
					
					result.appendChild(ingestMessageRequest);
					
					{
						final Document mdl = dbFactory.newDocumentBuilder().parse(new ByteArrayInputStream(processedMdl.getBytes(encoding)));
						Node messageElement = mdl.getDocumentElement();
						
						messageElement = result.adoptNode(messageElement);
						ingestMessageRequest.appendChild(messageElement);
						
						result.renameNode(messageElement, null, "message");
						result.renameNode(ingestMessageRequest, "http://mls.securboration.com/wsdl", "ingestMessageRequest");
					}
					
					String transformed = nodeToString(result);
					{//magic
						transformed = transformed.replace("<ingestMessageRequest xmlns=\"http://mls.securboration.com/wsdl\">", "<wsdlns:ingestMessageRequest xmlns:wsdlns=\"http://mls.securboration.com/wsdl\">");
						transformed = transformed.replace("</ingestMessageRequest>", "</wsdlns:ingestMessageRequest>");
					}//magic
					
					return transformed;
				}
    	} catch(Exception e) {
    		throw new RuntimeException(e);
    	}
	}
    
    /**
     * Emulates application-specific XML parsing logic (DOM API)
     * 
     * @param requestElement an XML document containing a single ingestMessageResponse element
     * @return 
     */
    private Object inboundMdlShim(
			final String requestElement
			) {
    	try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new ByteArrayInputStream(requestElement.getBytes(encoding)));
			
			final Element root = doc.getDocumentElement();
			
			final NodeList nodes = root.getChildNodes();
			if(nodes.getLength() == 0 || nodes.getLength() > 1) {
				return requestElement;
			}
			
			final Node message = nodes.item(0);
			
			if(!message.getNodeName().equals("message")) {
				return requestElement;
			}

			@mil.darpa.immortals.annotation.dsl.ontology.resources.xml.XmlDocument(xmlVersion="1.0", encoding="UTF-8", schemaNamespace="http://inetprogram.org/projects/MDL", schemaVersion = "17")
			final String processedMdl = processMdlReadFromServer(
					nodeToString(
							doc.renameNode(message, "http://inetprogram.org/projects/MDL", "MDLRoot")
							)
					);
			
			{
				final Document result = dbFactory.newDocumentBuilder().parse(new ByteArrayInputStream(requestElement.getBytes(encoding)));
				
				Document processedMdlDoc = dbFactory.newDocumentBuilder().parse(new ByteArrayInputStream(processedMdl.getBytes(encoding)));
				
				Element e = processedMdlDoc.getDocumentElement();
				processedMdlDoc.renameNode(e, "http://mls.securboration.com/wsdl", "message");
				
				final Node resultMessage = result.getDocumentElement().getFirstChild();
				
				resultMessage.getParentNode().removeChild(resultMessage);
				
				Node adopted = result.adoptNode(e);
				result.getDocumentElement().appendChild(adopted);
				
				final Unmarshaller u = jaxContext.createUnmarshaller();
				return u.unmarshal(new ByteArrayInputStream(nodeToString(result).getBytes(encoding)));
			}
    	} catch(Exception e) {
    		throw new RuntimeException(e);
    	}
	}
    
    private String nodeToString(Node doc) throws UnsupportedEncodingException, TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    
	    final ByteArrayOutputStream out = new ByteArrayOutputStream();
	    transformer.transform(new DOMSource(doc),new StreamResult(new OutputStreamWriter(out, encoding)));
	    
	    return new String(out.toByteArray(),StandardCharsets.UTF_8);
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
    		) throws IOException {
    	final String soapMessage = soapTemplate.replace("${FRAGMENT}", xmlFragment);
    	
//    	System.out.println("*** " + soapMessage.replace("\r\n", ""));
    	
    	final File outputDir = new File(messageDir,"interaction-"+messageCounter.incrementAndGet());
    	
    	final byte[] xmlPayload = soapMessage.getBytes(encoding);
    	
    	FileUtils.writeBytesToFile(
				outputDir.getAbsolutePath(), 
				"fragment.xml", 
				xmlFragment.getBytes(StandardCharsets.UTF_8)
				);
    	
    	FileUtils.writeBytesToFile(
				outputDir.getAbsolutePath(), 
				"request.xml", 
				xmlPayload
				);
    	
    	final byte[] xmlResponse = httpRequest(
    			"POST",
    			serverUrl,
    			xmlPayload,
    			"Content-Type", "text/xml; charset=" + encoding,
    			"SOAPAction",actionUri != null ? actionUri : ""
    			);
    	
    	FileUtils.writeBytesToFile(
				outputDir.getAbsolutePath(), 
				"response.xml", 
				xmlResponse
				);
    	
//    	{
//    		final File outputDir = new File(messageDir,"interaction-"+messageCounter.incrementAndGet());
//    		
//    		FileUtils.writeBytesToFile(
//    				outputDir.getAbsolutePath(), 
//    				"request.xml", 
//    				xmlPayload
//    				);
//    		
//    		FileUtils.writeBytesToFile(
//    				outputDir.getAbsolutePath(), 
//    				"response.xml", 
//    				xmlResponse
//    				);
//    	}
    	
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
                
                ByteArrayOutputStream err = new ByteArrayOutputStream();
                
                if(connection.getErrorStream() != null) {
	                try(InputStream errStream = connection.getErrorStream()){
	                	copy(errStream, err);
	                }
                }
                
                if(responseCode != 200){
                    throw new RuntimeException(
                        "for " + desc +
                        " response was " + responseCode + 
                        " but expected 200, with message \"" + connection.getResponseMessage() + "\" and error \"" + new String(err.toByteArray(),encoding) + "\""
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