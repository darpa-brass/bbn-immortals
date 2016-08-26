package com.securboration.immortals.service.init;

import java.util.Arrays;

import javax.servlet.Filter;

import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import com.securboration.immortals.instantiation.bytecode.UriMappings;
import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
import com.securboration.immortals.repo.api.QueryableRepository;
import com.securboration.immortals.repo.api.RepositoryConfiguration;
import com.securboration.immortals.repo.api.RepositoryUnsafe;
import com.securboration.immortals.service.api.ImmortalsRepositoryService;
import com.securboration.immortals.service.config.ImmortalsServiceProperties;

/**
 * This is the spring entry point for the application.
 * 
 * @author jstaples
 *
 */
@Configuration
@ComponentScan({ "com.securboration.immortals.service" })
@EnableAutoConfiguration
public class SpringEntryPoint 
        extends SpringBootServletInitializer
        implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SpringEntryPoint.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(
            SpringApplicationBuilder application) {
        return application.sources(SpringEntryPoint.class);
    }

    public static class ApplicationContextGetter
            implements ApplicationContextAware {
        public static volatile ApplicationContext c;

        @Override
        public void setApplicationContext(ApplicationContext c)
                throws BeansException {
            ApplicationContextGetter.c = c;
        }
    }

    @Bean
    public ObjectToTriplesConfiguration getObjectToTriplesConfig() {
        final String version = getProperties().getImmortalsVersion();

        ObjectToTriplesConfiguration config = 
                new ObjectToTriplesConfiguration(version);

        final String targetNamespace = "http://darpa.mil/immortals/ontology/"
                + version;

        config.setNamespaceMappings(
                Arrays.asList(
                        targetNamespace + "# IMMoRTALS", 
                        targetNamespace + "/edu/vanderbilt/immortals/models/deployment/com/securboration/test# deployment_spec"
                        ));

        config.setTargetNamespace(targetNamespace);

        config.setOutputFile(null);

        config.setTrimPrefixes(
                Arrays.asList(
                        "com/securboration/immortals/ontology",
                        "edu/vanderbilt/immortals/models"
                        ));

        return config;
    }
    
    @Bean
    public Filter hiddenHttpMethodFilter() {
        HiddenHttpMethodFilter filter = new HiddenHttpMethodFilter();
        return filter;
    }

    @Bean
    public RepositoryUnsafe getUnsafeRepository() {
        return new RepositoryUnsafe(getRepositoryConfiguration());
    }

    @Bean
    public QueryableRepository getQueryableRepository() {
        return new QueryableRepository(getRepositoryConfiguration());
    }

    @Bean
    public RepositoryConfiguration getRepositoryConfiguration() {
        ImmortalsServiceProperties properties = getProperties();

        RepositoryConfiguration c = new RepositoryConfiguration();
        c.setRepositoryBaseUrl(properties.getFusekiEndpointUrl());

        return c;
    }

    @Bean
    public UriMappings getUriMappings() {
        ObjectToTriplesConfiguration config = getObjectToTriplesConfig();

        UriMappings mappings = new UriMappings(config);

        return mappings;
    }

    @Bean
    public ImmortalsServiceProperties getProperties() {
        return new ImmortalsServiceProperties();
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.printf(
                "running %s\n",
                ImmortalsRepositoryService.class.getSimpleName());
    }

}
