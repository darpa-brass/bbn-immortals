package com.securboration.server;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.Source;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
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

import com.securboration.ftpc.FtpClientUploader;

import schemavalidator.BadnessReport;
import schemavalidator.SchemaComplianceChecker;

@EnableWs
@Configuration
public class Config extends WsConfigurerAdapter {
	
	@Value("${ftp.host}")
	private String ftpHost;
	
	@Value("${ftp.remoteFileName}")
	private String ftpRemoteFileName;
	
	@Value("${ftp.user}")
	private String ftpUser;
	
	@Value("${ftp.password}")
	private char[] ftpPassword;
	
	@Value("${ftp.xml-precheck:true}")//defaults to TRUE
	private boolean ftpXmlPrecheck;
	
	
	@Bean(name="tmpSchemaDir")
	public File getTmpSchemaDir() throws IOException {
		return createTmpSchemaDir();
	}
	
	@Bean
	public FtpClientUploader getFtpClientUploader() {
		return new FtpClientUploader(ftpHost,ftpRemoteFileName,ftpUser,ftpPassword);
	}
	
    
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
        };
        
        CommonsXsdSchemaCollection schemas = new CommonsXsdSchemaCollection(
            schemaResources
            );
        
        schemas.setInline(true);
        
        return schemas;
    }
    
    /**
     * Intercept the request before it can reach the ingestMessage method
     * 
     */
    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {

    	System.out.printf(
    			"NOTE: XML prechecking %s be performed before initiating an FTP upload\n", 
    			ftpXmlPrecheck ? "WILL" : "WILL NOT"
    				);
    	
    	if(!ftpXmlPrecheck) {
    		return;
    	}
        
    	interceptors.add(new EndpointInterceptor() {

			@Override
			public boolean handleRequest(
					MessageContext messageContext, 
					Object endpoint
					) throws Exception {
				
				try {
					
					Source s = messageContext.getRequest().getPayloadSource();
					
					BadnessReport report = SchemaComplianceChecker.getDocumentBadnessScore(
							getTmpSchemaDir(),
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
				} catch(Exception e) {
					e.printStackTrace();
					throw e;
				}
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
    
    private static File createTmpSchemaDir() throws IOException {
    	
    	final File tmpDir = new File("./tmp/schema");
    	
    	FileUtils.forceMkdir(tmpDir);
    	FileUtils.cleanDirectory(tmpDir);
    	
    	final Set<String> locations = new LinkedHashSet<>();
    	collectImportedNamespaces(
    			"wsdl/",
    			"wsdl/MessageListenerSchema.xsd",
    			locations
    			);
    	
    	if(locations.size() == 0) {
    		throw new RuntimeException("no /wsdl dir found on classpath");
    	}
    	
    	for(String loc:locations) {
    		ClassPathResource r = new ClassPathResource(loc);
    		
    		File f = new File(tmpDir,loc);
    		
    		FileUtils.writeStringToFile(
    				f,
    				IOUtils.toString(r.getInputStream(),StandardCharsets.UTF_8),
    				StandardCharsets.UTF_8
    				);
    	}
    	
    	return new File(tmpDir,locations.iterator().next()).getParentFile();
    }
    
    private static final Pattern pattern = Pattern.compile("\\\".*?.xsd\\\"");
    
    private static void collectImportedNamespaces(
    		final String resourceRoot,
    		final String current,
    		final Set<String> importLocations
    		) throws IOException{
    	final boolean isNew = importLocations.add(current);
    	
    	if(!isNew) {
    		return;
    	}
    	
    	System.out.printf("collecting imports from current %s: %s\n", current, importLocations);
    	
    	final String content = IOUtils.toString(new ClassPathResource(current).getInputStream(),StandardCharsets.UTF_8);
    	
    	final Matcher m = pattern.matcher(content);
    	
    	while(m.find()) {
    		final int start = m.start()+1;
    		final int end = m.end()-1;
    		
    		final String importPath = resourceRoot + content.substring(start,end);
    		collectImportedNamespaces(resourceRoot,importPath,importLocations);
    	}
    }
    
}


