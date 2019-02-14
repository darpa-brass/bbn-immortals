package com.securboration.immortals.bca.example.example1;

import java.io.IOException;

public class Driver {
    
    public static void driver() throws IOException{
        
        MonolithicAtakApplication app = new MonolithicAtakApplication();
        
        test();
        
//        for(int i=0;i<10;i++)
        {
            app.run();
            System.out.println();
        }
        
    }
    
    private static void test(){};

}
