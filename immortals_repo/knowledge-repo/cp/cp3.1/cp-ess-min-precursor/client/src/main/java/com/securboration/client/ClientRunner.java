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
