//package com.securboration.immortals.test.scratchpad.composition;
//
//import com.securboration.immortals.test.scratchpad.composition.Annotations.ControlPoint;
//import com.securboration.immortals.test.scratchpad.composition.Annotations.LoggingDfu;
//
///**
// * Example of a simple application that logs messages 
// * 
// * @author Securboration
// *
// */
//public class Example1 {
//    
//    /**
//     * Drives the demo
//     * 
//     * @param args
//     * @throws Exception
//     */
//    public static void main(String[] args) throws Exception {
//        new Application1().runMockApplication();
//    }
//    
//    private static class Application1{
//        
//        @ControlPoint(LoggingDfu.class)
//        private Dfus.LoggerWrapper1 logger = new Dfus.LoggerWrapper1();
//        
//        private void runMockApplication() throws Exception{
//            
//            //... application logic goes here
//            {
//                for(String m:Misc.getMessages()){
//                    logger.logMessage(m);
//                }
//            }
//        }
//        
//    }
//    
//    
//    
//    
//    
//
//    
//}
