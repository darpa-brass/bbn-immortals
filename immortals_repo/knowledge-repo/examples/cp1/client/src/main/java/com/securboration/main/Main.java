package com.securboration.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.securboration.client.Config;

@SpringBootApplication(scanBasePackageClasses=Config.class)
public class Main {
    
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }

//    @Bean
//    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
//        return new CommandLineRunner() {
//
//            @Override
//            public void run(String... args) throws Exception {
//                trueMain(args);
//            }
//            
//        };
//    }
//    
//    private static void trueMain(String...args) throws Exception{
//        final String serverUri = "http://localhost:8080/ws/";
//        
//        MessageListenerClient client = new MessageListenerClient(serverUri);
//        
//        System.out.println(client.ping().getDelta());
//    }

}
