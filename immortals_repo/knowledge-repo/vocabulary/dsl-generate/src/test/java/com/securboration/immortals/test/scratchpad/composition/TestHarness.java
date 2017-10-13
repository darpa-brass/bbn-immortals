//package com.securboration.immortals.test.scratchpad.composition;
//
//public class TestHarness {
//    
//    static{
//        System.setProperty(
//                "java.util.logging.SimpleFormatter.format",
//                "%4$-7s \"%5$s\"%n"
//                );
//    }
//    
//    public static void main(String[] args) throws Exception{
//        System.out.println("running default application:");
//        Example1.main(args);
//        
//        System.out.println("\nrunning synthesized application variant 1 (block cipher that leaks information):");
//        Example1AfterSynthesis.main(args);
//        
//        System.out.println("\nrunning synthesized application variant 2 (stream cipher):");
//        Example2AfterSynthesis.main(args);
//    }
//
//}
