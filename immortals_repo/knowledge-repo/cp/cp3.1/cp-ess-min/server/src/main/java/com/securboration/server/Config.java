package com.securboration.server;


import java.io.File;
import java.util.List;

import javax.xml.transform.Source;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadValidatingInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.XsdSchemaCollection;
import org.springframework.xml.xsd.commons.CommonsXsdSchemaCollection;

import schemavalidator.BadnessReport;
import schemavalidator.SchemaComplianceChecker;

@EnableWs
@Configuration
public class Config extends WsConfigurerAdapter {
    
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
            ApplicationContext applicationContext
            ) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        
        return new ServletRegistrationBean<>(servlet, "/ws/*");
    }

    @Bean(name = "messageListener")
    public DefaultWsdl11Definition defaultWsdl11Definition() {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        
        wsdl11Definition.setPortTypeName("MessageListenerPort");
        wsdl11Definition.setLocationUri("/ws");
        wsdl11Definition.setTargetNamespace("http://mls.securboration.com/wsdl");
        
        wsdl11Definition.setSchemaCollection(wsdlSchemas());
        
        return wsdl11Definition;
    }
    
    @Bean
    public DefaultMethodEndpointAdapter adapter() {
        return new DefaultMethodEndpointAdapter();
    }
    
    @Bean 
    public XsdSchemaCollection wsdlSchemas() {
        final Resource[] schemaResources = {
                new ClassPathResource("wsdl/MessageListenerSchema.xsd"),//this imports everything else
                
//                new ClassPathResource("wsdl/MDL_v0_8_19.xsd"),
//                new ClassPathResource("wsdl/Tmats.xsd"),
//                new ClassPathResource("wsdl/TmatsBGroup.xsd"),
//                new ClassPathResource("wsdl/TmatsCGroup.xsd"),
//                new ClassPathResource("wsdl/TmatsCommonTypes.xsd"),
//                new ClassPathResource("wsdl/TmatsDGroup.xsd"),
//                new ClassPathResource("wsdl/TmatsGGroup.xsd"),
//                new ClassPathResource("wsdl/TmatsHGroup.xsd"),
//                new ClassPathResource("wsdl/TmatsMGroup.xsd"),
//                new ClassPathResource("wsdl/TmatsPGroup.xsd"),
//                new ClassPathResource("wsdl/TmatsRGroup.xsd"),
//                new ClassPathResource("wsdl/TmatsSGroup.xsd"),
//                new ClassPathResource("wsdl/TmatsTGroup.xsd"),
//                new ClassPathResource("wsdl/TmatsVGroup.xsd")
        };
        
        CommonsXsdSchemaCollection schemas = new CommonsXsdSchemaCollection(
            schemaResources
            );
        
        schemas.setInline(true);
        
        return schemas;
    }
    
    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {
        //this interceptor validates that messages conform to the schema
        

    	interceptors.add(new EndpointInterceptor() {

			@Override
			public boolean handleRequest(
					MessageContext messageContext, 
					Object endpoint
					) throws Exception {
				Source s = messageContext.getRequest().getPayloadSource();
				
				BadnessReport report = SchemaComplianceChecker.getDocumentBadnessScore(
						new File("./schema/server").getCanonicalFile(),
//						new File("C:\\Users\\Securboration\\Desktop\\code\\immortals\\trunk\\knowledge-repo\\cp\\cp3.1\\cp-ess-min\\etc\\schemas\\v2"), 
						s
						);
				
	            if(report.getBadnessScore() > 0) {
	            	System.out.println("found a fault " + messageContext.getClass().getName());
	            	System.out.println("badness = " + report.getBadnessScore());
					System.out.println(report);
		            System.out.println();
	            	
	            	throw new RuntimeException(report.toString());
	            }
				
				return true;
			}

			@Override
			public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
				return true;
			}

			@Override
			public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
				
				return true;
			}

			@Override
			public void afterCompletion(MessageContext messageContext, Object endpoint, Exception ex) throws Exception {
				
			}
    		
    	});
    	
        PayloadValidatingInterceptor validatingInterceptor = new PayloadValidatingInterceptor();
        validatingInterceptor.setValidateRequest(true);
        validatingInterceptor.setValidateResponse(true);
        validatingInterceptor.setXsdSchemaCollection(wsdlSchemas());
        interceptors.add(validatingInterceptor);
    }
    
}


