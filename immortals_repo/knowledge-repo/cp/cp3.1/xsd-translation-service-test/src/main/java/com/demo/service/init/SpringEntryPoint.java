package com.demo.service.init;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import com.securboration.schemavalidator.main.Main;

/**
 * This is the spring entry point for the application.
 * 
 * @author jstaples
 *
 */
@EnableAutoConfiguration
public class SpringEntryPoint implements CommandLineRunner {
    
    private static final long classTime = System.currentTimeMillis();
    private final long constructorTime = System.currentTimeMillis();

    public static void main(String[] args) {
        SpringApplication.run(SpringEntryPoint.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        System.out.printf(
                "running %s, startup took (tot=%dms,config=%dms)\n",
                Main.class.getSimpleName(),
                System.currentTimeMillis() - classTime,
                System.currentTimeMillis() - constructorTime
                );
        
        Main.main(args);
    }

}
