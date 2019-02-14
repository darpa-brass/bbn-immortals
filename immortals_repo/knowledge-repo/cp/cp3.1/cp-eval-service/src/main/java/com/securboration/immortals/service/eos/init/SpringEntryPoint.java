package com.securboration.immortals.service.eos.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.securboration.immortals.adapt.engine.AdaptationEngine;
import com.securboration.immortals.bridge.Bridge.MockEvaluationBridge;
import com.securboration.immortals.service.eos.impl.EvaluationFsm;
import com.securboration.immortals.swri.EvaluationProperties;
import com.securboration.immortals.swri.EvaluationProperties.EvaluationPropertyKey;
import com.securboration.immortals.swri.SwriEvaluationHelper;

import mil.darpa.immortals.schemaevolution.ChallengeProblemBridge;

/**
 * This is the spring entry point for the application.
 * 
 * @author jstaples
 *
 */
@Configuration
@ComponentScan({
    "com.securboration.immortals.service", 
    "com.securboration.immortals.adapt.config" 
    })
@EnableAutoConfiguration
public class SpringEntryPoint 
        extends SpringBootServletInitializer
        implements CommandLineRunner {
    
    private static final long classTime = System.currentTimeMillis();
    private final long constructorTime = System.currentTimeMillis();

    public static void main(String[] args) {
        SpringApplication.run(
            SpringEntryPoint.class, 
            args
            );
    }

    @Override
    protected SpringApplicationBuilder configure(
            SpringApplicationBuilder application
            ) {
        return application.sources(SpringEntryPoint.class);
    }
    
    @Bean
    public EvaluationFsm getEvaluationFsm(AdaptationEngine a){
        return new EvaluationFsm(a);
    }
    
    @Bean
    public AdaptationEngine getAdaptationEngine(){
        return new AdaptationEngine();
    }
    
    @Bean
    public EvaluationProperties getEvaluationProperties(){
        return new EvaluationProperties(new String[]{});
    }
    
    @Bean
    public ChallengeProblemBridge getEvaluationBridge() throws Exception{
        final EvaluationProperties properties = getEvaluationProperties();
        
        if(properties.get(EvaluationPropertyKey.evalType).equals("live")){
            return new ChallengeProblemBridge();
        }
        
        return new MockEvaluationBridge(properties);
    }
    
    
    @Autowired(required=true)
    private EvaluationProperties evaluationProperties;
    
    @Autowired(required=true)
    private ChallengeProblemBridge evaluationBridge;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.printf(
                "running %s, startup took (tot=%dms,config=%dms)\n",
                this.getClass().getSimpleName(),
                System.currentTimeMillis() - classTime,
                System.currentTimeMillis() - constructorTime
                );
        
        try{
            SwriEvaluationHelper.evaluate(evaluationProperties,evaluationBridge);
            System.exit(0);
        } catch(Throwable t){
            t.printStackTrace();
            System.exit(-1);
        }
    }

}
