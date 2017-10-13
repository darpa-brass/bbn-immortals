package com.securboration.immortals.deployment.Main;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.securboration.immortals.deployment.parser2.ParserImpl;
import com.securboration.immortals.deployment.pojos.ObjectInstance;

/**
 * 
 * 
 * 
 * @author jstaples
 *
 */
public class Main3_1 {
    
    public static void main(String[] args) throws IOException, IllegalArgumentException, IllegalAccessException{
        final String pathToDeploymentJson =
                "src/test/resources/immortals_dm_test.json";
        
        final String json = 
                FileUtils.readFileToString(new File(pathToDeploymentJson));
        
        ParserImpl parser = new ParserImpl();
        parser.parse(json);
        
        
        for(ObjectInstance o:parser.getInstances()){
            printObject(o);
            System.out.println();
        }
    }
    
    public static void printObject(Object o){
        System.out.printf("%s\n", o);
    }

}
