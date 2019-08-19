package com.securboration.server;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.inetprogram.projects.mdl.MDLRootType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.securboration.ftpc.FtpClientUploader;
import com.securboration.mls.wsdl.IngestMessageRequest;
import com.securboration.mls.wsdl.IngestMessageResponse;
import com.securboration.mls.wsdl.PingRequest;
import com.securboration.mls.wsdl.PingResponse;



@Endpoint
public class ServerEndpoint {
    
    private static final String NS = 
            "http://mls.securboration.com/wsdl";
    
    @Autowired
    private FtpClientUploader ftpUploader;
    
    @PayloadRoot(
        namespace = NS, 
        localPart = "pingRequest"
        )
    @ResponsePayload
    public PingResponse ping(
            @RequestPayload 
            PingRequest request
            ) {
        PingResponse response = new PingResponse();
        response.setDelta(System.currentTimeMillis() - request.getTimestamp());
        return response;
    }
    
    @PayloadRoot(
        namespace = NS, 
        localPart = "ingestMessageRequest"
        )
    @ResponsePayload
    public IngestMessageResponse ingestMessage(
            @RequestPayload
            IngestMessageRequest ingestMessageRequest
            ) throws Exception {
    	System.out.println("received an ingestMessage request");
    	
    	try {
    		ftpTee(ingestMessageRequest);
    		
    		//echo back the request to the client
    		IngestMessageResponse response = new IngestMessageResponse();
    		response.setMessage(ingestMessageRequest.getMessage());
    		return response;
    	} catch(Exception e) {
    		e.printStackTrace();
    		throw e;
    	}
    }
    
    private void ftpTee(IngestMessageRequest request) throws Exception {
    	final String mdlString = MdlHelper.getMdl(request);
    	
    	final File f = new File("./tmp.mdl");
    	FileUtils.writeStringToFile(f, mdlString, StandardCharsets.UTF_8);
    	
    	ftpUploader.upload(f);
    }
    
    private static class MdlHelper{
    	
    	//jank-o-matic 9000
    	
    	private static String print(Node n) throws TransformerConfigurationException, TransformerException {
    		final TransformerFactory tf = TransformerFactory.newInstance();
    		
            final StringWriter sw = new StringWriter();
            
            final Transformer t = tf.newTransformer();
            
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(n), new StreamResult(sw));
            
            return sw.getBuffer().toString();
    	}
    	
    	private static Document toDocument(
    			final IngestMessageRequest request
    			) throws JAXBException, SAXException, IOException, ParserConfigurationException {
    		final JAXBContext jaxbContext = JAXBContext.newInstance(IngestMessageRequest.class);
            
            StringWriter test = new StringWriter();
            jaxbContext.createMarshaller().marshal(request, test);
            
            final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            
            dbf.setNamespaceAware(true);
            dbf.setIgnoringComments(false);
            
            return dbf.newDocumentBuilder().parse(
            		new ByteArrayInputStream(test.toString().getBytes())
            		);
    	}
    	
    	private static Node getChild(Node n, String childName) throws TransformerConfigurationException, TransformerException {
    		NodeList children = n.getChildNodes();
    		
    		for(int i=0;i<children.getLength();i++) {
    			Node child = children.item(i);
    			
    			if(child.getNodeName().endsWith(childName)) {
    				return child;
    			}
    		}
    		
    		System.out.println(print(n));//TODO
    		
    		throw new RuntimeException("unable to find child named " + childName);
    	}
    	
    	
    	private static String getMdl(final IngestMessageRequest request) throws Exception {
    		final Document d = toDocument(request);
    		
    		final Node ingestMessageRequestElement = getChild(d, ":ingestMessageRequest");
    		final Node messageElement = getChild(ingestMessageRequestElement, "message");
    		
    		final Node renamed = d.renameNode(messageElement, "http://inetprogram.org/projects/MDL", "MDLRoot");
    		
    		final Document newDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    		
    		newDoc.adoptNode(renamed);
    		
    		return print(renamed);
    	}
    	
    }

}
