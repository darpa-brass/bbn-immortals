package com.securboration.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.securboration.server.Config;

@SpringBootApplication(scanBasePackageClasses=Config.class)
public class Main {
    
    public static void main(String[] args) throws Exception {
    	SpringApplication.run(Main.class, args);
    }

}
