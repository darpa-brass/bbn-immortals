//package com.securboration.immortals.test.scratchpad.composition;
//
//import java.lang.annotation.Repeatable;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.Base64;
//import java.util.Random;
//
//import javax.crypto.SecretKey;
//import javax.crypto.spec.SecretKeySpec;
//
///**
// * These are fake, hand-generated annotations that emulate what we expect to see
// * in the annotation DSL
// * 
// * @author Securboration
// *
// */
//public class Annotations {
//
//    public static class RandomByteGenerator{
//        static final Random rng = new Random();
//        
//        static byte[] generateRandomBytes(final int length){
//            byte[] data = new byte[length];
//            
//            rng.nextBytes(data);
//            
//            return data;
//        }
//    }
//    
//    public static SecretKey getKey(){
//        final byte[] secretKey = 
//                Base64.getDecoder().decode("vN+VU2kvbQHcVqDf57tUsA==");
//        return new SecretKeySpec(secretKey, 0, secretKey.length, "AES"); 
//    }
//    
//    public static String hash(byte[] data) throws NoSuchAlgorithmException{
//        MessageDigest hasher = MessageDigest.getInstance("SHA-256");
//        hasher.update(data);
//        return Base64.getEncoder().encodeToString(hasher.digest());
//    }
//    
//    
//    
//    //hard coded annotation DSL just for demo purposes
//    
//    public static class Data{
//    }
//    public static class EncryptionKey extends Data{}
//    public static class Message extends Data{}
//    public static class BinaryData extends Data{}
//    
//    public static class Property{
//    }
//    public static class Encrypted extends Property{}
//    public static class BlockBased extends Property{}
//    public static class StreamBased extends Property{}
//    
//    public static class LosslessTransformation extends Property{
//    }
//    public static class InvertibleTransformation extends Property{
//    }
//    
//    public @interface FunctionalProperties{
//        public FunctionalProperty[] value();
//    }
//    
//    @Repeatable(value = FunctionalProperties.class)
//    public @interface FunctionalProperty{
//        public Class<? extends Property> value();
//    }
//    
//    public @interface DataProperty{
//        public Class<? extends Property> value();
//    }
//    
//    public @interface DataType{
//        public Class<? extends Data> value();
//    }
//    
//    public class Text extends Data{}
//    
//    public @interface DataStructure{
//        
//    }
//    
//    public @interface Dfu{
//        public Class<? extends Functionality> value();
//    }
//    
//    public static class Functionality{}
//    
//    public static class Cipher extends Functionality{}
//    public static class LoggingDfu extends Functionality{}
//    
//    public @interface ControlPoint{
//        
//        public Class<? extends Functionality> value();
//        
//    }
//    
//    public @interface SynthesizedByImmortals{}
//    public @interface ModifiedByImmortals{}
//    
//    
//}
