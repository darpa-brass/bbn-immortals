package com.demo.service.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.demo.service.api.XsdTranslationService;

/**
 * This is the spring entry point for the application.
 * 
 * @author jstaples
 *
 */
@Configuration
@ComponentScan({ "com.demo.service" })
@EnableAutoConfiguration
public class SpringEntryPoint 
        extends SpringBootServletInitializer
        implements CommandLineRunner {
    
    private static final long classTime = System.currentTimeMillis();
    private final long constructorTime = System.currentTimeMillis();

    public static void main(String[] args) {
        SpringApplication.run(SpringEntryPoint.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(
            SpringApplicationBuilder application
            ) {
        return application.sources(SpringEntryPoint.class);
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.printf(
                "running %s, startup took (tot=%dms,config=%dms)\n",
                XsdTranslationService.class.getSimpleName(),
                System.currentTimeMillis() - classTime,
                System.currentTimeMillis() - constructorTime
                );
    }

}
