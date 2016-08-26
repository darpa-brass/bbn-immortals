//package com.securboration.immortals.test.scratchpad.composition;
//
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.security.SecureRandom;
//import java.util.Arrays;
//import java.util.Base64;
//import java.util.Collection;
//import java.util.Random;
//
//import javax.crypto.SecretKey;
//import javax.crypto.spec.IvParameterSpec;
//import javax.crypto.spec.SecretKeySpec;
//
//public class Misc {
//    
//    public static Collection<String> getMessages(){
//        return Arrays.asList(
//                "started application",
//                "message",
//                "message",
//                "message",
//                "about to shut down application"
//                );
//    }
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
//    public static class ThirdPartyBlockCipher {
//        
//
//        private static final IvParameterSpec initialVector = getInitialVector();
//        
//        private final javax.crypto.Cipher aes;
//        
//        public ThirdPartyBlockCipher(
//                SecretKey key,
//                int mode
//                ) throws Exception {
//            aes = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
//            aes.init(mode, key, initialVector);
//        }
//        
//        public byte[] encrypt(byte[] data) throws Exception {
//            return aes.doFinal(data);
//        }
//    }
//    
//    public static class ThirdPartyStreamCipher {
//        
//        private static final IvParameterSpec initialVector = getInitialVector();
//
//        private final javax.crypto.Cipher aes;
//        
//        public ThirdPartyStreamCipher(
//                SecretKey key,
//                int mode
//                ) throws Exception {
//            aes = javax.crypto.Cipher.getInstance("AES/CTR/NoPadding");
//            aes.init(mode, key, initialVector);
//        }
//        
//        public byte[] streamEncrypt(byte[] data){
//            return aes.update(data);
//        }
//    }
//    
//    public static IvParameterSpec getInitialVector() {
//        byte[] initVector = new byte[16];
//        SecureRandom random = new SecureRandom(new byte[]{});
//        random.nextBytes(initVector);
//        return new IvParameterSpec(initVector);
//    }
//    
//    public static class ImmortalsSynthesizedLogicException extends RuntimeException{
//        private static final long serialVersionUID = 1L;
//    }
//    
//}
