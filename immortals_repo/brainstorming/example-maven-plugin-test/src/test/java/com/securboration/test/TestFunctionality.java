package com.securboration.test;

import java.io.IOException;

import com.securboration.immortals.test.Main;

import junit.framework.TestCase;

public class TestFunctionality extends TestCase{
    
    public TestFunctionality(String name){
        super(name);
    }
    
    public void testFunctionality() throws IOException{
        Main.main(new String[]{});
    }
    
}
