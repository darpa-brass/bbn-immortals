package com.securboration.scratchpad.cipher;

import java.io.ByteArrayOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.securboration.scratchpad.cipher.EncryptionExample.CryptoImpls.CryptoImpl1;
import com.securboration.scratchpad.cipher.EncryptionExample.CryptoImpls.CryptoImpl2;
import com.securboration.scratchpad.cipher.EncryptionExample.CryptoImpls.CryptoImpl3;

/**
 * All-in-one example illustrating some of the difficulties associated with
 * defining a Spec for consuming Cipher DFUs.
 * 
 * Ciphers are interesting because:
 * 1) there is a rich history of new algorithms being added over time
 * 2) there are a variety of third-party cipher implementations
 * 3) ciphers require nontrivial configuration
 * 4) ciphers have nontrivial usage patterns
 * 
 * @author jstaples
 *
 */
public class EncryptionExample {
    
    public static void main(String[] args) throws Exception{
        Application.main(args);
    }
    
    
    /**
     * Simulation of different libraries for performing encryption/decryption
     */
    public static class CryptoImpls{
        
        /**
         * A simple Cipher implementation that internally uses javax.crypto
         */
        public static class CryptoImpl1{
            
            private final String algorithm;
            private final String chainingMode;
            private final String paddingScheme;
            
            private final SecretKeySpec keySpec;
            private final IvParameterSpec initVectorSpec;
            
            //e.g., AES,CBC,PKCS5PADDING
            public CryptoImpl1(
                    final String algorithm, 
                    final int keyLengthBytes,
                    final String chainingMode, 
                    final String paddingScheme,
                    
                    final String keyPhrase,
                    final String initVectorPhrase
                    ) throws NoSuchAlgorithmException{
                this.algorithm = algorithm;
                this.chainingMode = chainingMode;
                this.paddingScheme = paddingScheme;
                
                this.keySpec = getKeySpec(keyPhrase,algorithm,keyLengthBytes);
                this.initVectorSpec = getInitVectorSpec(initVectorPhrase,keyLengthBytes);
            }
            
            private Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException{
                return Cipher.getInstance(
                    algorithm + "/" + chainingMode + "/" + paddingScheme
                    );
            }
            
            private static byte[] hash(
                    String phrase, 
                    int lengthBytes
                    ) throws NoSuchAlgorithmException{
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(phrase.getBytes());
                
                return Arrays.copyOf(hash, lengthBytes);
            }
            
            private static SecretKeySpec getKeySpec(
                    final String keyPhrase,
                    final String algorithm,
                    final int keyLengthBytes
                    ) throws NoSuchAlgorithmException{
                SecretKeySpec keySpec = new SecretKeySpec(
                    hash(keyPhrase,keyLengthBytes),
                    algorithm
                    );
                
                return keySpec;
            }
            
            private static IvParameterSpec getInitVectorSpec(
                    final String initVectorPhrase,
                    final int lengthBytes
                    ) throws NoSuchAlgorithmException{
                return new IvParameterSpec(
                    hash(initVectorPhrase,lengthBytes)
                    );
            }
            
            public byte[] encrypt(
                    final byte[] dataToEncrypt
                    ) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
                Cipher cipher = getCipher();
                
                cipher.init(
                    Cipher.ENCRYPT_MODE, 
                    keySpec, 
                    initVectorSpec
                    );

                return cipher.doFinal(dataToEncrypt);
            }

            public byte[] decrypt(
                    final byte[] encryptedData
                    ) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
                Cipher cipher = getCipher();
                
                cipher.init(
                    Cipher.DECRYPT_MODE, 
                    keySpec, 
                    initVectorSpec
                    );

                return cipher.doFinal(encryptedData);
            }

        }
        
        /**
         * Another Cipher implementation with a slightly different API
         */
        public static class CryptoImpl2{
            private String algorithm;
            private String chainingMode;
            private String paddingScheme;
            
            private SecretKeySpec keySpec;
            private IvParameterSpec initVectorSpec;
            
            //e.g., AES,CBC,PKCS5PADDING
            public CryptoImpl2(){}
            
            public void init(
                    final String algorithm, 
                    final int keyLengthBytes,
                    final String chainingMode, 
                    final String paddingScheme,
                    
                    final String keyPhrase,
                    final String initVectorPhrase
                    ) throws NoSuchAlgorithmException{
                this.algorithm = algorithm;
                this.chainingMode = chainingMode;
                this.paddingScheme = paddingScheme;
                
                this.keySpec = getKeySpec(keyPhrase,algorithm,keyLengthBytes);
                this.initVectorSpec = getInitVectorSpec(initVectorPhrase,keyLengthBytes);
            }
            
            private Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException{
                return Cipher.getInstance(
                    algorithm + "/" + chainingMode + "/" + paddingScheme
                    );
            }
            
            private static byte[] hash(
                    String phrase, 
                    int lengthBytes
                    ) throws NoSuchAlgorithmException{
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(phrase.getBytes());
                
                return Arrays.copyOf(hash, lengthBytes);
            }
            
            private static SecretKeySpec getKeySpec(
                    final String keyPhrase,
                    final String algorithm,
                    final int keyLengthBytes
                    ) throws NoSuchAlgorithmException{
                SecretKeySpec keySpec = new SecretKeySpec(
                    hash(keyPhrase,keyLengthBytes),
                    algorithm
                    );
                
                return keySpec;
            }
            
            private static IvParameterSpec getInitVectorSpec(
                    final String initVectorPhrase,
                    final int lengthBytes
                    ) throws NoSuchAlgorithmException{
                return new IvParameterSpec(
                    hash(initVectorPhrase,lengthBytes)
                    );
            }
            
            public byte[] encrypt(
                    final byte[] dataToEncrypt
                    ) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
                Cipher cipher = getCipher();
                
                cipher.init(
                    Cipher.ENCRYPT_MODE, 
                    keySpec, 
                    initVectorSpec
                    );

                return cipher.doFinal(dataToEncrypt);
            }

            public byte[] decrypt(
                    final byte[] encryptedData
                    ) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
                Cipher cipher = getCipher();
                
                cipher.init(
                    Cipher.DECRYPT_MODE, 
                    keySpec, 
                    initVectorSpec
                    );

                return cipher.doFinal(encryptedData);
            }
        }
        
        /**
         * An AES-specific Cipher implementation
         */
        public static class CryptoImpl3{
            private String algorithm;
            private String chainingMode;
            private String paddingScheme;
            
            private SecretKeySpec keySpec;
            private IvParameterSpec initVectorSpec;
            
            private final Cipher cipher;
            
            public CryptoImpl3(
                    final String keyPhrase
                    ) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException{
                init(
                    "AES",
                    16,
                    "CBC",
                    "PKCS5PADDING",
                    keyPhrase,
                    UUID.randomUUID().toString()
                    );
                
                this.cipher = getCipher();
            }
            
            public void encryptMode() throws InvalidKeyException, InvalidAlgorithmParameterException{
                cipher.init(
                    Cipher.ENCRYPT_MODE, 
                    keySpec, 
                    initVectorSpec
                    );
            }
            
            public void decryptMode() throws InvalidKeyException, InvalidAlgorithmParameterException{
                cipher.init(
                    Cipher.DECRYPT_MODE, 
                    keySpec, 
                    initVectorSpec
                    );
            }
            
            public byte[] put(byte[] chunk){
                return cipher.update(chunk);
            }
            
            public byte[] finish() throws IllegalBlockSizeException, BadPaddingException{
                return cipher.doFinal();
            }
            
            private void init(
                    final String algorithm, 
                    final int keyLengthBytes,
                    final String chainingMode, 
                    final String paddingScheme,
                    
                    final String keyPhrase,
                    final String initVectorPhrase
                    ) throws NoSuchAlgorithmException{
                this.algorithm = algorithm;
                this.chainingMode = chainingMode;
                this.paddingScheme = paddingScheme;
                
                this.keySpec = getKeySpec(keyPhrase,algorithm,keyLengthBytes);
                this.initVectorSpec = getInitVectorSpec(initVectorPhrase,keyLengthBytes);
            }
            
            private Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException{
                return Cipher.getInstance(
                    algorithm + "/" + chainingMode + "/" + paddingScheme
                    );
            }
            
            private static byte[] hash(
                    String phrase, 
                    int lengthBytes
                    ) throws NoSuchAlgorithmException{
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(phrase.getBytes());
                
                return Arrays.copyOf(hash, lengthBytes);
            }
            
            private static SecretKeySpec getKeySpec(
                    final String keyPhrase,
                    final String algorithm,
                    final int keyLengthBytes
                    ) throws NoSuchAlgorithmException{
                SecretKeySpec keySpec = new SecretKeySpec(
                    hash(keyPhrase,keyLengthBytes),
                    algorithm
                    );
                
                return keySpec;
            }
            
            private static IvParameterSpec getInitVectorSpec(
                    final String initVectorPhrase,
                    final int lengthBytes
                    ) throws NoSuchAlgorithmException{
                return new IvParameterSpec(
                    hash(initVectorPhrase,lengthBytes)
                    );
            }
        }
    }
    
    private static class CipherConfig{
        private final String tag;
        private final String algorithm;
        private final int keyLength;

        private CipherConfig(String tag, String algorithm, int keyLength) {
            super();
            this.tag = tag;
            this.algorithm = algorithm;
            this.keyLength = keyLength;
        }
    }
    
    private enum CipherConfigs{
        AES_128("AES",16),
        DES("DES",8),
        BLOWFISH("BLOWFISH",8),
        ;
        
        private final CipherConfig config;

        private CipherConfigs(final String algorithm, final int keyLength) {
            this.config = new CipherConfig(this.name(),algorithm,keyLength);
        }
        
        public CipherConfig getConfig() {
            return config;
        }
    }

    /**
     * Illustrates an application that consumes three different Cipher DFUs. In
     * reality, we'd start with an application consuming just one Cipher
     * implementation and then adapt it to use another, but here an enumeration
     * is provided to highlight the differences between implementations
     *
     */
    private static class Application {

        private static void main(String[] args) throws Exception {
            
            //data to encrypt
            final byte[] dataToEncrypt = "This is test data".getBytes();
            
            //key
            final String keyPhrase = "APasswordPhrase";
            
            //initialization vector
            final String initVectorPhrase = "AnInitVectorPhrase";
            
            //cipher configuration
            final CipherConfig c = CipherConfigs.AES_128.getConfig();
            final String cipherAlgorithm = c.algorithm;
            final int keyLengthBytes = c.keyLength;
            final String chainingMode = "CBC";
            final String paddingScheme = "PKCS5PADDING";
            
            //encrypt using CryptoImpl1
            {
                //initialize the cipher
                CryptoImpl1 cipher = new CryptoImpl1(
                    cipherAlgorithm,
                    keyLengthBytes,
                    chainingMode,
                    paddingScheme,
                    keyPhrase,
                    initVectorPhrase
                    );
                
                //perform an encryption and then a decryption
                final byte[] encryptedData = cipher.encrypt(dataToEncrypt);
                final byte[] recoveredData = cipher.decrypt(encryptedData);
                
                //sanity checking and logging
                sanityCheck(
                    cipher.getClass(),
                    c.tag,
                    dataToEncrypt,
                    encryptedData,
                    recoveredData
                    );
            }
            
            //encrypt using CryptoImpl2
            {
                /*
                 * CryptoImpl2 is very similar to CryptoImpl1 except it
                 * has a different initialization pattern.  Whereas all of the
                 * configuration is provided in the constructor for CryptoImpl1,
                 * CryptoImpl2 accepts these parameters in its init() method.
                 */
                
                //initialize the cipher
                CryptoImpl2 cipher = new CryptoImpl2();
                cipher.init(
                    cipherAlgorithm,
                    keyLengthBytes,
                    chainingMode,
                    paddingScheme,
                    keyPhrase,
                    initVectorPhrase
                    );
                
                //perform an encryption and then a decryption
                final byte[] encryptedData = cipher.encrypt(dataToEncrypt);
                final byte[] recoveredData = cipher.decrypt(encryptedData);
                
                //sanity checking and logging
                sanityCheck(
                    cipher.getClass(),
                    c.tag,
                    dataToEncrypt,
                    encryptedData,
                    recoveredData
                    );
            }
            
            //encrypt using CryptoImpl3
            {
                /*
                 * Note that unlike the other crypto impls, here we don't need
                 * to define an algorithm because the algorithm is hard coded
                 * in the implementation.
                 * 
                 * Also note that this impl is stream-based (i.e., operates on
                 * chunks of data instead of encrypting a monolithic array
                 * of bytes all at once).
                 */
                
                //initialize the cipher
                CryptoImpl3 cipher = new CryptoImpl3(keyPhrase);
                
                ByteArrayOutputStream cryptStream = new ByteArrayOutputStream();
                ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                
                //encrypt
                {
                    cipher.encryptMode();
                    cryptStream.write(cipher.put(dataToEncrypt));
                    cryptStream.write(cipher.finish());
                }
                
                final byte[] encryptedData = cryptStream.toByteArray();
                
                //decrypt
                {
                    cipher.decryptMode();
                    dataStream.write(cipher.put(encryptedData));
                    dataStream.write(cipher.finish());
                }
                
                final byte[] recoveredData = dataStream.toByteArray();
                
                //sanity checking and logging
                sanityCheck(
                    cipher.getClass(),
                    "custom crypto alg",
                    dataToEncrypt,
                    encryptedData,
                    recoveredData
                    );
            }
            
            
        }
        
        private static void sanityCheck(
                final Class<?> impl,
                final String algorithmName, 
                final byte[] input, 
                final byte[] crypt, 
                final byte[] output
                ){
            System.out.println("\n"+impl.getSimpleName() + ":" + algorithmName);
            System.out.println("\tinput:  " + new String(input));
            System.out.println("\tcrypt:  " + Base64.getEncoder().encodeToString(crypt));
            System.out.printf("\toutput: " + new String(output));
            
            Assertions.assertEqual(input, output);
            Assertions.assertNotEqual(input, crypt);
            Assertions.assertNotEqual(output, crypt);
            
            System.out.printf(" \u2713\n");
        }
        
        

    }
    
    private static class Assertions {
        private static boolean areEqual(byte[] b1, byte[] b2){
            if(b1 == b2){
                return true;
            }
            
            if(b1 == null || b2 == null){
                return false;
            }
            
            if(b1.length != b2.length){
                return false;
            }
            
            for(int i=0;i<b1.length;i++){
                if(b1[i] != b2[i]){
                    return false;
                }
            }
            
            return true;
        }
        
        private static void assertEqual(byte[] b1, byte[] b2){
            if(areEqual(b1,b2)){
                return;
            }
            
            fail();
        }
        
        private static void assertNotEqual(byte[] b1, byte[] b2){
            if(!areEqual(b1,b2)){
                return;
            }
            
            fail();
        }
        
        private static void fail(){
            throw new RuntimeException("assertion failed");
        }
        
        private static void assertTrue(boolean b){
            if(!b){
                fail();
            }
        }
    }

}
