//package com.securboration.immortals.test.scratchpad.composition;
//
//import javax.crypto.SecretKey;
//
//import com.securboration.immortals.test.scratchpad.composition.Annotations.Cipher;
//import com.securboration.immortals.test.scratchpad.composition.Annotations.ControlPoint;
//import com.securboration.immortals.test.scratchpad.composition.Annotations.LoggingDfu;
//import com.securboration.immortals.test.scratchpad.composition.Dfus.CipherWrapperStream;
//
///**
// * Modification to Example1 that illustrates the changes that must be made to
// * support an emergent requirement that all LoggingDfu outputs have the
// * Encrypted property.
// * 
// * Note that encrypting each line in the log separately like this is bad because
// * dictionary attacks. Ideally a secure stream cipher would be used.
// * 
// * @author Securboration
// *
// */
//public class Example2AfterSynthesis {
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
//    private static class /* Modified by IMMoRTALS */ Application1{
//        
//        @ControlPoint(LoggingDfu.class)
//        private Dfus.LoggerWrapper1 logger = new Dfus.LoggerWrapper1();
//        
//        /* Modified by IMMoRTALS */
//        /* TODO: incomplete placeholder synthesis logic */
//        private SecretKey key = Misc.getKey();
//        
//        /* Added by IMMoRTALS */
//        @ControlPoint(Cipher.class)
//        private CipherWrapperStream cipher = new CipherWrapperStream(key);
//        
//        private void runMockApplication() throws Exception{
//            
//            //... application logic goes here
//            {
//                for(String m:Misc.getMessages()){
//                    logger.logMessage(encrypt(cipher,key,m));
//                }
//            }
//            
//        }
//        
//    }
//    
//    /* DFU adapter added by IMMoRTALS */
//    private static String encrypt(CipherWrapperStream c,SecretKey k,String s){
//        try {
//            return new String(c.encrypt(getBytes(s)));
////            return Base64.getEncoder().encodeToString(c.encrypt(k, getBytes(s)));
//        } catch (Exception e) {
//            throw new RuntimeException();
//        }
//    }
//    
//    /* POJO type converter added by IMMoRTALS */
//    private static byte[] getBytes(String s){
//        return s.getBytes();
//    }
//    
//    
//    
//    
//    
//
//    
//}
