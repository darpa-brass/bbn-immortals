package com.securboration.client;


import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class Config {

    @Bean("soapMarshaller")
    public Jaxb2Marshaller soapMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setSchemas(new Resource[] {new ClassPathResource("wsdl/MessageListenerSchema.xsd")});
        marshaller.setPackagesToScan(new String[] {"com.securboration.mls.wsdl"});
        return marshaller;
    }
    
    @Bean("inputMarshaller")
    public Jaxb2Marshaller inputMarshaller() throws IOException {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setSchemas(new Resource[] {new ClassPathResource("wsdl/" + getMdlSchemaXsd())});
        marshaller.setPackagesToScan(new String[] {"org.inetprogram.projects.mdl"});
        return marshaller;
    }

    @Bean
    public MessageListenerClient client(
            @Qualifier("soapMarshaller")
            Jaxb2Marshaller serverMarshaller
            ) {
        MessageListenerClient client = new MessageListenerClient(Key.SERVER_ENDPOINT_URL.getValue());
//        client.setDefaultUri("http://localhost:8080/ws");
        client.setMarshaller(serverMarshaller);
        client.setUnmarshaller(serverMarshaller);
        return client;
    }
    
    @Bean("expectedMdlSchemaVersion")
    private static String getMdlSchemaXsd() throws IOException {
        return IOUtils.toString(
            new ClassPathResource("wsdl/schemaVersion.dat").getInputStream(), 
            StandardCharsets.UTF_8
            ).trim().replace("\n", "").replace("\r", "");
    }
    
}


