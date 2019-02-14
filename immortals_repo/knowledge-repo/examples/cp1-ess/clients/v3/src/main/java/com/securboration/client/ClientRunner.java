package com.securboration.client;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBElement;

import org.apache.commons.io.FileUtils;
import org.inetprogram.projects.mdl.MDLRootType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import org.springframework.xml.transform.StringSource;

import com.securboration.client.test.Report;

@Component
public class ClientRunner {
    
    @Autowired
    private MessageListenerClient client;
    
    @Autowired
    @Qualifier("inputMarshaller")
    private Jaxb2Marshaller marshaller;
    
    @Autowired
    @Qualifier("expectedMdlSchemaVersion")
    private String expectedMdlSchemaVersion;
    
    @PostConstruct
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
                FileUtils.writeStringToFile(new File(Key.REPORT_DIR.getValue(),"report.dat"), reportValue, StandardCharsets.UTF_8.name());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        
        
        
    }
    
    @SuppressWarnings("unchecked")//erasure :(
    private static <T> T get(Map<String,Object> map, String key){
        return (T) map.get(key);
    }
    
    private void clientActionInternal(Report report) throws IOException {
        {
            System.out.println("client injected @" + client.getDefaultUri());
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
        for(File messageToSend:FileUtils.listFiles(messagesDir, new String[] {"xml"}, true)) {
            testCount++;
            final String testName = "test" + testCount;
            
            report.put(testName + " input", messageToSend.getAbsolutePath());
            
            System.out.println(messageToSend.getAbsolutePath());
            
            try {
                final String content = FileUtils.readFileToString(
                    messageToSend,
                    StandardCharsets.UTF_8.name()
                    );
                
                clientAction(content);
                
                report.put(testName + " result", "test PASSED");
                
                passCount++;
            } catch(Exception e) {
                e.printStackTrace();
                
                report.put(testName + " result", "test FAILED due to an exception of type " + e.getClass().getName() + " with message " + e.getMessage());
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
    
    private void clientAction(String xmlInput) {
        
        JAXBElement<?> message = (JAXBElement)marshaller.unmarshal(new StringSource(xmlInput));
        
        if(!message.getDeclaredType().equals(MDLRootType.class)) {
            nuke("unmarshalled document is not MDL (it's a " + message.getDeclaredType().getName() + ")");
        }
        
        MDLRootType mdl = (MDLRootType) message.getValue();
        
        {
            //TODO: application-specific business logic using the mdl as input
        }
        
        client.ingestMessage(mdl);
    }
    
    private void nuke(String message) {
        throw new RuntimeException(message);
    }

    
    public MessageListenerClient getClient() {
        return client;
    }

    
    public void setClient(MessageListenerClient client) {
        this.client = client;
    }
    
    
    
    
}
