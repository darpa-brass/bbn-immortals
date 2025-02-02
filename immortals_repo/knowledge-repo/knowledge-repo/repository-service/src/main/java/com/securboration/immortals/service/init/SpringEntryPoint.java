package com.securboration.immortals.service.init;

import java.util.Arrays;

import javax.persistence.EntityManagerFactory;
import javax.servlet.Filter;
import javax.sql.DataSource;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.flywaydb.core.Flyway;
import org.h2.server.web.WebServlet;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.*;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
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
            SpringApplicationBuilder application
            ) {
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
    public ServletRegistrationBean h2servletRegistration() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new WebServlet());
        registration.addUrlMappings("/console/*");
        registration.addInitParameter("webAllowOthers", "true");
        return registration;
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
    
    @ConditionalOnProperty(value = "sqlDatabase.enabled")
    @Bean(initMethod = "migrate")
    Flyway flyway() {
        Flyway flyway = new Flyway();
        flyway.setDataSource(dataSource());
        flyway.setSchemas("db_example");
        flyway.setLocations("/db/migration");
        flyway.clean();
        flyway.setValidateOnMigrate(false);
        flyway.migrate();
        return flyway;
    }
    @ConditionalOnProperty(value = "sqlDatabase.enabled")
    @Bean @DependsOn("flyway")
    EntityManagerFactory entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean bean = new
                LocalContainerEntityManagerFactoryBean();
        bean.setDataSource(dataSource());
        return bean.getObject();
    }
    @ConditionalOnProperty(value = "sqlDatabase.enabled")
    @Bean
    DataSource dataSource() {
        MysqlDataSource dataSource = new MysqlDataSource();
        dataSource.setURL("jdbc:mysql://localhost:3306/db_example");
        dataSource.setUser("root");
        dataSource.setPassword(ROOT_PASS);
        return dataSource;
    } 
    
    @Override
    public void run(String... args) throws Exception {
        System.out.printf(
                "running %s\n",
                ImmortalsRepositoryService.class.getSimpleName());
    }
    
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        if (!registry.hasMappingForPattern("/webjars/**")) {
//            registry.addResourceHandler("/webjars/**").addResourceLocations(
//                    "classpath:/META-INF/resources/webjars/");
//        }
//        if (!registry.hasMappingForPattern("/**")) {
//            registry.addResourceHandler("/**").addResourceLocations(
//                    RESOURCE_LOCATIONS);
//        }
//    }
    
    
//    @Bean
//    public Resolver getResolver(){
//        return new Resolver();
//    }
//    
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//       boolean devMode = this.env.acceptsProfiles("dev");
//       boolean useResourceCache = !devMode;
//       Integer cachePeriod = devMode ? 0 : null;
//
//       registry.addResourceHandler("/public/**")
//          .addResourceLocations("/public/", "classpath:/public/")
//          .setCachePeriod(cachePeriod)
//          .resourceChain(useResourceCache)
//          .addResolver(new GzipResourceResolver())
//          .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"))
//          .addTransformer(new AppCacheManifestTransformer()
//              );
//    }
//    
//    
//    private static class Resolver implements ResourceResolver{
//        
//        public Resolver(){
//            System.out.println("creating resolver...\n");
//        }
//        
//        @Override
//        public Resource resolveResource(
//                HttpServletRequest request,
//                String requestPath, 
//                List<? extends Resource> locations,
//                ResourceResolverChain chain
//                ) {
//            
//            System.out.printf("resolveResource: %s\n",request.getRequestURI());//TODO
//            
//            return chain.resolveResource(request, requestPath, locations);
//        }
//
//        @Override
//        public String resolveUrlPath(
//                String resourcePath,
//                List<? extends Resource> locations, 
//                ResourceResolverChain chain
//                ) {
//            
//            System.out.printf("resolvePath: %s\n",resourcePath);//TODO
//            
//            return chain.resolveUrlPath(resourcePath, locations);
//        }
//    }

    private static String ROOT_PASS = "";

}
