package com.securboration.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.OpenOption;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import mil.darpa.immortals.annotation.dsl.ontology.resources.xml.XmlInstance;
import org.inetprogram.projects.mdl.MDLRootType;

import com.securboration.client.test.Report;
import com.securboration.mls.wsdl.IngestMessageRequest;
import com.securboration.mls.wsdl.IngestMessageResponse;
import com.securboration.mls.wsdl.PingRequest;
import com.securboration.mls.wsdl.PingResponse;

public class ClientRunner {
    
	private String expectedMdlSchemaVersion;
    
	
    private final MessageListenerClient client;
    
    private final JAXBContext jaxContext;
    
    public ClientRunner() throws JAXBException {
    	JAXBContext jaxbContext = JAXBContext.newInstance(
    			IngestMessageRequest.class, 
    			IngestMessageResponse.class,
    			
    			PingRequest.class,
    			PingResponse.class
    			);
    	
    	this.jaxContext = jaxbContext;
    	
    	this.client = new MessageListenerClient(
    			Key.SERVER_ENDPOINT_URL.getValue(),
    			jaxbContext
    			);
    }
    
    public void clientAction() {
        
        final Report report = new Report();
        final long startTime = System.currentTimeMillis();
        
        try {
            clientActionInternal(report);
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally {
            final long endTime = System.currentTimeMillis();
            report.put("overall elapsed time (millis)", endTime - startTime);
            
            final String reportValue = report.toString();
            
            System.out.println(reportValue);
            
            try {
                FileUtils.writeStringToFile(
                		Key.REPORT_DIR.getValue(),"report.dat", 
                		reportValue, 
                		StandardCharsets.UTF_8
                		);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        
        
        
    }
    
    private static int countSubstringOccurrences(final String s, final String substring) {
    	if(!s.contains(substring)) {
    		return 0;
    	}
    	
    	int index = 0;
    	int count = 0;
    	
    	boolean stop = false;
    	while(!stop) {
    		int matchIndex = s.indexOf(substring, index);
    		
    		if(matchIndex < 0) {
    			stop = true;
    		} else {
    			index = matchIndex + substring.length();
    			count++;
    		}
    	}
    	
    	return count;
    }
    
    
    private void clientActionInternal(Report report) throws IOException {
        {
            System.out.println("client injected @" + Key.SERVER_ENDPOINT_URL.getValue());
            System.out.printf("warming up connection\n");
            for(int i=0;i<10;i++) {
                System.out.printf("client ping is %dms\n",client.ping().getDelta());
            }
        }
        
        {
            report.put("server URI", Key.SERVER_ENDPOINT_URL.getValue());
            report.put("server ping (millis)", client.ping().getDelta());
            report.put("expected MDL schema version",expectedMdlSchemaVersion);
        }
        
        final File messagesDir = new File(Key.MESSAGES_TO_SEND_DIR.getValue());
        

        report.put("starting test run with input dir",messagesDir.getAbsolutePath());
        
        int testCount = 0;
        int passCount = 0;
        
        double totalBadnessWeighted = 0d;
        double totalBadnessNormalized = 0d;
        double badElements = 0d;
        for(int i=0;i<3;i++) {//TODO
        	System.out.println("test " + i);//TODO
        for(File messageToSend:FileUtils.listFiles(messagesDir, "xml")) {
            testCount++;
            final String testName = "test" + testCount;
            
            report.put(testName + " input", messageToSend.getAbsolutePath());
            
            System.out.println(messageToSend.getAbsolutePath());
            
            try {
                final String content = FileUtils.readFileToString(
                    messageToSend,
                    StandardCharsets.UTF_8
                    );
                
                clientAction(processMdlFromFileSystem(content));
                
                report.put(testName + " result", "test PASSED");
                
                passCount++;
            } catch(Exception e) {
                e.printStackTrace();
                
                {
                	final String s = e.toString();
//                	final String s = "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\"><SOAP-ENV:Header/><SOAP-ENV:Body><SOAP-ENV:Fault><faultcode>SOAP-ENV:Server</faultcode><faultstring xml:lang=\"en\">1 errors/fatals found in document:  FATAL @line 33 \"&lt;ns3:DeliveryClass&gt;BestEffort&lt;/ns3:DeliveryClass&gt;\" in 1-line element spanning [33,33] \"&lt;ns3:DeliveryClass&gt;BestEffort&lt;/ns3:DeliveryClass&gt;\" ... \"&lt;ns3:DeliveryClass&gt;BestEffort&lt;/ns3:DeliveryClass&gt;\" with message \"cvc-complex-type.2.4.a: Invalid content was found starting with element 'ns3:DeliveryClass'. One of '{\"http://inetprogram.org/projects/MDL\":ProperName}' is expected.\"document's [normalized, weighted] badness scores are [0.0017, 1.0000]</faultstring></SOAP-ENV:Fault></SOAP-ENV:Body></SOAP-ENV:Envelope>";
                	
                	final String sigil = "document's [normalized, weighted] badness scores are [";
                	
                	if(s.contains(sigil)) {
                		final String suffix = s.substring(s.lastIndexOf(sigil));
                		
                		final String[] parts = suffix.split("badness scores are ");
                		final String scores = parts[1].substring(1,parts[1].indexOf("]"));
                		
                		final String[] scoresSplit = scores.split(",");
                		
                		final double normalizedScore = Double.parseDouble(scoresSplit[0]);
                		final double weightedScore = Double.parseDouble(scoresSplit[1]);
                		
                		totalBadnessWeighted += weightedScore;
                		totalBadnessNormalized += normalizedScore;
                	}
                	
                	badElements += countSubstringOccurrences(s, "FATAL ");
                }
                
                report.put(testName + " result", "test FAILED due to an exception of type " + e.getClass().getName() + " with message " + e.getMessage());
            }
        }
        }
        
        report.put("done with tests");
        report.put("# tests performed", testCount);
        report.put("# tests that passed", passCount);
        report.put("# tests that failed", testCount - passCount);
        report.put("pass rate", testCount == 0 ? 1d : (1d*passCount)/(1d*testCount));
        report.put("fail rate", testCount == 0 ? 0d : (1d*(testCount - passCount))/(1d*testCount));
        
        report.put("score", testCount == 0 ? 1d : (1d*passCount)/(1d*testCount));
        
        report.put("mean bad document fraction (lower is better)", testCount == 0 ? 0d : totalBadnessNormalized/(1d*testCount));
        report.put("mean bad lines per document (lower is better)", testCount == 0 ? 0d : totalBadnessWeighted/(1d*testCount));
        report.put("mean bad elements per document (lower is better)",testCount == 0 ? 0d : badElements/(1d*testCount));
    }
    
    /**
	 * 
	 * @param mdl an XML document containing a single MDLRoot element
	 * @return a possibly modified version of the input. An XML document containing
	 *         a single MDLRoot element.
	 */
    @mil.darpa.immortals.annotation.dsl.ontology.resources.xml.XmlDocument(
    		xmlVersion="1.0",
    		encoding="UTF-8",
    		schemaNamespace="http://inetprogram.org/projects/MDL"
    		)
    private String processMdlFromFileSystem(
    		@mil.darpa.immortals.annotation.dsl.ontology.resources.xml.XmlDocument(
    	    		xmlVersion="1.0",
    	    		encoding="UTF-8",
    	    		schemaNamespace="http://inetprogram.org/projects/MDL"
    	    		)
    		final String mdl
    		) {
    	return mdl;
    }
    
    private void clientAction(
    		String xmlInput
    		) throws IOException, JAXBException {
        
        JAXBElement<?> message = unmarshal(xmlInput,MDLRootType.class);
        
        if(!message.getDeclaredType().equals(MDLRootType.class)) {
            nuke("unmarshalled document is not MDL (it's a " + message.getDeclaredType().getName() + ")");
        }
        
        MDLRootType mdl = (MDLRootType) message.getValue();
        
        {
            //TODO: application-specific business logic using the mdl as input
        }
        
        IngestMessageResponse response = client.ingestMessage(mdl);
        System.out.println("RECEIVED XML RESPONSE");
        processResponse(response);
    }

    private void processResponse(IngestMessageResponse response) {
        System.out.println("PROCESSING RESPONSE");
    }
    
    private void nuke(String message) {
        throw new RuntimeException(message);
    }
    
    @XmlInstance
    private JAXBElement<?> unmarshal(
    		String xml, 
    		Class<?> type
    		) throws JAXBException {
        Unmarshaller jaxbUnmarshaller = jaxContext.createUnmarshaller();
        return jaxbUnmarshaller.unmarshal(new StreamSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))), type);
    }
    
}
