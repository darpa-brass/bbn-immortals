//package com.securboration.immortals.adapt.config;
//
//import java.util.Arrays;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import com.securboration.immortals.o2t.ObjectToTriplesConfiguration;
//import com.securboration.immortals.repo.api.QueryableRepository;
//import com.securboration.immortals.repo.api.RepositoryConfiguration;
//import com.securboration.immortals.repo.api.RepositoryUnsafe;
//
//@Configuration
//public class AdaptationBeans {
//
//    @Bean
//    public ObjectToTriplesConfiguration getObjectToTriplesConfig() {
//        final String version = getAdaptationEngineProperties().getImmortalsVersion();
//
//        ObjectToTriplesConfiguration config = 
//                new ObjectToTriplesConfiguration(version);
//
//        final String targetNamespace = "http://darpa.mil/immortals/ontology/"
//                + version;
//
//        config.setNamespaceMappings(
//                Arrays.asList(
//                        targetNamespace + "# IMMoRTALS", 
//                        targetNamespace + "/edu/vanderbilt/immortals/models/deployment/com/securboration/test# deployment_spec"
//                        ));
//
//        config.setTargetNamespace(targetNamespace);
//
//        config.setOutputFile(null);
//
//        config.setTrimPrefixes(
//                Arrays.asList(
//                        "com/securboration/immortals/ontology",
//                        "edu/vanderbilt/immortals/models"
//                        ));
//
//        return config;
//    }
//
//    @Bean
//    public RepositoryUnsafe getUnsafeRepository() {
//        return new RepositoryUnsafe(getRepositoryConfiguration());
//    }
//
//    @Bean
//    public QueryableRepository getQueryableRepository() {
//        return new QueryableRepository(getRepositoryConfiguration());
//    }
//
//    @Bean
//    public RepositoryConfiguration getRepositoryConfiguration() {
//        AdaptationEngineProperties properties = getAdaptationEngineProperties();
//
//        RepositoryConfiguration c = new RepositoryConfiguration();
//        c.setRepositoryBaseUrl(properties.getFusekiEndpointUrl());
//
//        return c;
//    }
//
//    @Bean
//    public AdaptationEngineProperties getAdaptationEngineProperties() {
//        return new AdaptationEngineProperties();
//    }
//}
