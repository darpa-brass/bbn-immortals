package com.securboration.immortals.swri.eval;

import java.io.PrintStream;

public class Out {
    
    private static long startTime = System.currentTimeMillis();

    
    private static int elapsedSeconds(){
        return (int)((System.currentTimeMillis() - startTime)/1000L);
    }
    
    public static void println(
            PrintStream printer,
            String tag,
            String format, 
            Object...args
            ){
        final String formattedTag = String.format("%-20s", tag);
        
        format = format.replace("\n", "\n[" + formattedTag + "]           ");
        printer.printf(
            "[%s][@T+%3ds]  %s\n", 
            formattedTag,
            elapsedSeconds(), 
            String.format(format, args)
            );
    }
    
    
    public static void println(
            String tag,
            String format, 
            Object...args
            ){
        println(System.out,tag,format,args);
    }

}
