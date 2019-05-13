package com.securboration.main;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.securboration.server.Config;

@SpringBootApplication(scanBasePackageClasses=Config.class)
public class Main {
    
    public static void main(String[] args) throws Exception {
    	startProcessKiller();
    	
        SpringApplication.run(Main.class, args);
    }
    
    private static void startProcessKiller() {
    	Thread t = new Thread() {
    		public void run() {
    			while(true) {
    				try {
    					File killFile = new File("./killServer.dat");
    					
    					if(killFile.exists()) {
    						System.out.printf(
    								"detected kill signal %s, terminating\n", 
    								killFile.getAbsolutePath()
    								);
    						
    						FileUtils.forceDelete(killFile);
    						
    						System.exit(0);
    					}
    					
    					Thread.sleep(1000L);
    					
    				} catch(Exception e) {
    					e.printStackTrace();
    					System.exit(-1);
					}
				}
			}
    	};
    	
    	t.setDaemon(true);
    	t.setName("cp-ess-server-killer");
    	t.start();
    }

}
