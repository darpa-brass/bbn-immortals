package com.securboration.immortals.instantiation.annotationparser.bytecode;


public class Console {
    
    public static void log(String format,Object...args){
        //throw new RuntimeException("this shouldn't be used in production code");//TODO
        
        System.out.println(String.format(format, args));
    }

}
