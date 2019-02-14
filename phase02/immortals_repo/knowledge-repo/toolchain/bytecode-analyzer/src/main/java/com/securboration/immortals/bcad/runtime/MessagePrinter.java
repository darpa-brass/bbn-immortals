package com.securboration.immortals.bcad.runtime;


public class MessagePrinter {
    
    public static void print(String s){
        System.out.printf(
            "[%10s]  %s\n", 
            Thread.currentThread().getName(),
            s
            );
    }

}
