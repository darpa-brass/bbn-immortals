package com.securboration.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.inetprogram.projects.mdl.MDLRootType;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceMessageExtractor;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.springframework.ws.support.MarshallingUtils;
import org.springframework.xml.transform.StringResult;
import org.springframework.xml.transform.StringSource;

import com.securboration.mls.wsdl.IngestMessageRequest;
import com.securboration.mls.wsdl.IngestMessageResponse;
import com.securboration.mls.wsdl.PingRequest;
import com.securboration.mls.wsdl.PingResponse;

public class MessageListenerClient extends WebServiceGatewaySupport {
    
    public MessageListenerClient(String serverEndpointUri) {
        serviceUri = serverEndpointUri;
    }
    
    private final String serviceUri;
    private final String pingActionUri = "http://mls.securboration.com/wsdl/ping";
    private final String ingestActionUri = "http://mls.securboration.com/wsdl/ingestMessage";
            
    
    public PingResponse ping() {
        PingRequest request = new PingRequest();
        request.setTimestamp(System.currentTimeMillis());
        
        
        PingResponse response = (PingResponse) 
                getWebServiceTemplate().marshalSendAndReceive(
                    serviceUri, 
                    request,
                    new SoapActionCallback(pingActionUri)
                    );
        
        return response;
    }
    
    public IngestMessageResponse ingestMessage(
            MDLRootType message
            ) {
        IngestMessageRequest request = new IngestMessageRequest();
        request.setMessage(message);
        
        final boolean usePassthrough = true;
        
        if(usePassthrough) {
	        return (IngestMessageResponse) marshalSendAndReceive(
	        		getWebServiceTemplate(),
	    			serviceUri,
	    			request,
	    			null
	    			);
        } else {
        	return (IngestMessageResponse) 
	                getWebServiceTemplate().marshalSendAndReceive(
	                    serviceUri, 
	                    request,
	                    new SoapActionCallback(ingestActionUri)
	                    );
        }
        
        
    }
    
    private String magicalBlackBox(String inputXml) {
    	try {
	    	final String translatedXml = translate(NOOP_XSL,inputXml);
	    	
	    	//TODO: use XSLT to transform the input document
	    	
//	    	System.out.println(inputXml);//TODO
//	    	System.out.println(translatedXml);//TODO
	    	
	    	System.out.println("does input equal (.equals) noopXsl(input) ? : " + inputXml.equals(translatedXml));
	    	
	    	return inputXml;
    	} catch(JAXBException|TransformerException e) {
    		throw new RuntimeException(e);
    	}
    }
    
    private static String translate(
    		final String xslt, 
    		final String xml
    		) throws JAXBException, TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer(new StreamSource(new ByteArrayInputStream(xslt.getBytes())));
        
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        transformer.transform(
        		new StreamSource(new ByteArrayInputStream(xml.getBytes())), 
        		new StreamResult(out)
        		);
        
        return out.toString();
	}
    
	private Object marshalSendAndReceive(
			final WebServiceTemplate template,
			final String uri,
			final Object requestPayload,
			final WebServiceMessageCallback requestCallback
			) {
		final String xml = toXml(requestPayload);
		
		final String transformedXml = magicalBlackBox(xml);
		
		final Object resultantRequestPayload = fromXml(transformedXml);
		
		return template.sendAndReceive(
				uri, 
				
				new WebServiceMessageCallback() {//request
					public void doWithMessage(WebServiceMessage request) throws IOException, TransformerException {
						if (resultantRequestPayload != null) {
							Marshaller marshaller = getMarshaller();
							if (marshaller == null) {
								throw new IllegalStateException(
										"No marshaller registered. Check configuration of WebServiceTemplate.");
							}
							MarshallingUtils.marshal(marshaller, resultantRequestPayload, request);
							if (requestCallback != null) {
								requestCallback.doWithMessage(request);
							}
						}
					}
				}, 
				
				new WebServiceMessageExtractor<Object>() {//response
					public Object extractData(WebServiceMessage response) throws IOException {
						Unmarshaller unmarshaller = getUnmarshaller();
						if (unmarshaller == null) {
							throw new IllegalStateException(
									"No unmarshaller registered. Check configuration of WebServiceTemplate.");
						}
						return MarshallingUtils.unmarshal(unmarshaller, response);
					}
				}
			);
	}
	
	private Object fromXml(String xml) {
		try {
			return getWebServiceTemplate().getUnmarshaller().unmarshal(new StringSource(xml));
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
    
    private String toXml(Object message) {
    	StringResult r = new StringResult();
    	
    	try {
			getWebServiceTemplate().getMarshaller().marshal(
					message, 
					r
					);
		} catch (XmlMappingException | IOException e) {
			throw new RuntimeException(e);
		}
    	
    	return r.toString();
    }
    
    private static final String NOOP_XSL = 
    		"<xsl:stylesheet version=\"1.0\"\r\n" + 
    		"	xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\"\r\n" + 
    		"	xmlns=\"http://test.com/\">\r\n" + 
    		"\r\n" + 
    		"<xsl:output method=\"xml\"/>\r\n" + 
    		"	<xsl:template match=\"@*|node()\">\r\n" + 
    		"		<xsl:copy>\r\n" + 
    		"			<xsl:apply-templates select=\"@*|node()\"/>\r\n" + 
    		"		</xsl:copy>\r\n" + 
    		"	</xsl:template>\r\n" + 
    		"</xsl:stylesheet>\r\n" + 
    		"";
}
