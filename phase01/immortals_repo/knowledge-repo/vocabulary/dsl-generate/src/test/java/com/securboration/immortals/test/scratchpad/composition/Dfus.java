//package com.securboration.immortals.test.scratchpad.composition;
//
//import java.util.logging.Level;
//
//import javax.crypto.SecretKey;
//
//import com.securboration.immortals.test.scratchpad.composition.Annotations.BinaryData;
//import com.securboration.immortals.test.scratchpad.composition.Annotations.BlockBased;
//import com.securboration.immortals.test.scratchpad.composition.Annotations.Cipher;
//import com.securboration.immortals.test.scratchpad.composition.Annotations.DataProperty;
//import com.securboration.immortals.test.scratchpad.composition.Annotations.DataType;
//import com.securboration.immortals.test.scratchpad.composition.Annotations.Dfu;
//import com.securboration.immortals.test.scratchpad.composition.Annotations.Encrypted;
//import com.securboration.immortals.test.scratchpad.composition.Annotations.EncryptionKey;
//import com.securboration.immortals.test.scratchpad.composition.Annotations.FunctionalProperty;
//import com.securboration.immortals.test.scratchpad.composition.Annotations.InvertibleTransformation;
//import com.securboration.immortals.test.scratchpad.composition.Annotations.LoggingDfu;
//import com.securboration.immortals.test.scratchpad.composition.Annotations.LosslessTransformation;
//import com.securboration.immortals.test.scratchpad.composition.Annotations.StreamBased;
//import com.securboration.immortals.test.scratchpad.composition.Annotations.Text;
//import com.securboration.immortals.test.scratchpad.composition.Misc.ThirdPartyBlockCipher;
//import com.securboration.immortals.test.scratchpad.composition.Misc.ThirdPartyStreamCipher;
//
//import mil.darpa.immortals.annotation.dsl.ontology.functionality.FunctionalAspect;
//
///**
// * Enumeration of known DFUs
// * 
// * @author Securboration
// *
// */
//public class Dfus{
//    /**
//     * A logging subsystem based on java/util/logging
//     * 
//     * @author Securboration
//     *
//     */
//    @Dfu(LoggingDfu.class)
//    public static class LoggerWrapper1{
//        
//        private java.util.logging.Logger logger = 
//                java.util.logging.Logger.getLogger("testLogger");
//        
//        @FunctionalAspect
//        public void logMessage(
//                @DataType(Text.class)
//                String message
//                ){
//            logger.log(Level.INFO, message);
//        }
//        
//    }
//    
//    /**
//     * A homebrew logging subsystem
//     * 
//     * @author Securboration
//     *
//     */
//    @Dfu(LoggingDfu.class)
//    public static class LoggerWrapper2{
//        
//        @FunctionalAspect
//        public void logMessage(
//                @DataType(Text.class)
//                String message
//                ){
//            System.out.println(message);
//        }
//        
//    }
//    
//    
//    /**
//     * An AES stream cipher with an encryption aspect
//     * 
//     * @author Securboration
//     *
//     */
//    @Dfu(Cipher.class)
//    @FunctionalProperty(StreamBased.class)
//    public static class CipherWrapperStream{
//        
//        private final ThirdPartyStreamCipher forwardCipher;
//        private final ThirdPartyStreamCipher inverseCipher;
//        
//        @FunctionalAspect
//        public CipherWrapperStream(
//                @DataType(EncryptionKey.class)
//                SecretKey key
//                ){
//            try {
//                forwardCipher = 
//                        new ThirdPartyStreamCipher(
//                                key,
//                                javax.crypto.Cipher.ENCRYPT_MODE
//                                );
//                inverseCipher = 
//                        new ThirdPartyStreamCipher(
//                                key,
//                                javax.crypto.Cipher.DECRYPT_MODE
//                                );
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//        
//        @FunctionalAspect
//        @FunctionalProperty(LosslessTransformation.class)
//        @FunctionalProperty(InvertibleTransformation.class)
//        public 
//            @DataType(BinaryData.class)
//            @DataProperty(Encrypted.class)
//            byte[] 
//                encrypt(
//                        @DataType(BinaryData.class)
//                        byte[] data
//                        ) throws Exception{
//            byte[] encrypted = forwardCipher.streamEncrypt(data);
//            byte[] middle = "  --[decrypted]-->  ".getBytes();
//            byte[] recovered = inverseCipher.streamEncrypt(encrypted);
//            
//            byte[] result = new byte[encrypted.length + middle.length + recovered.length];
//            
//            System.arraycopy(encrypted, 0, result, 0, encrypted.length);
//            System.arraycopy(middle, 0, result, encrypted.length, middle.length);
//            System.arraycopy(recovered, 0, result, middle.length + encrypted.length, recovered.length);
//            
//            return result;
//        }
//    }
//    
//    /**
//     * An AES block cipher with an encryption aspect
//     * 
//     * @author Securboration
//     *
//     */
//    @Dfu(Cipher.class)
//    @FunctionalProperty(BlockBased.class)
//    public static class CipherWrapperBlock{
//        
//        private final ThirdPartyBlockCipher forwardCipher;
//        private final ThirdPartyBlockCipher inverseCipher;
//        
//        @FunctionalAspect
//        public CipherWrapperBlock(
//                @DataType(EncryptionKey.class)
//                SecretKey key
//                ){
//            try {
//                forwardCipher = 
//                        new ThirdPartyBlockCipher(
//                                key,
//                                javax.crypto.Cipher.ENCRYPT_MODE
//                                );
//                inverseCipher = 
//                        new ThirdPartyBlockCipher(
//                                key,
//                                javax.crypto.Cipher.DECRYPT_MODE
//                                );
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//        
//        @FunctionalAspect
//        @FunctionalProperty(LosslessTransformation.class)
//        @FunctionalProperty(InvertibleTransformation.class)
//        public 
//            @DataType(BinaryData.class)
//            @DataProperty(Encrypted.class)
//            byte[] 
//                encrypt(
//                        @DataType(BinaryData.class)
//                        byte[] data
//                        ) throws Exception{
//            byte[] encrypted = forwardCipher.encrypt(data);
//            byte[] middle = "  --[decrypted]-->  ".getBytes();
//            byte[] recovered = inverseCipher.encrypt(encrypted);
//            
//            byte[] result = new byte[encrypted.length + middle.length + recovered.length];
//            
//            System.arraycopy(encrypted, 0, result, 0, encrypted.length);
//            System.arraycopy(middle, 0, result, encrypted.length, middle.length);
//            System.arraycopy(recovered, 0, result, middle.length + encrypted.length, recovered.length);
//            
//            
//            return result;
//        }
//    }
//}
//
