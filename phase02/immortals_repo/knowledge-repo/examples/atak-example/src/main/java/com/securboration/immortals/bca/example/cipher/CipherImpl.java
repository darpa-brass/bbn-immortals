package com.securboration.immortals.bca.example.cipher;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * A simple Cipher implementation that internally uses javax.crypto
 */
public class CipherImpl{
    
    private final String algorithm;
    private final String chainingMode;
    private final String paddingScheme;
    
    private final SecretKeySpec keySpec;
    private final IvParameterSpec initVectorSpec;
    
    //e.g., AES,CBC,PKCS5PADDING
    public CipherImpl(
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
    
    //streaming cipher API
    
    public CipherOutputStream acquire(OutputStream o) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException{
        
        Cipher cipher = getCipher();
        
        cipher.init(
            Cipher.ENCRYPT_MODE, 
            keySpec, 
            initVectorSpec
            );
        
        return new CipherOutputStream(o,cipher);
    }
    
    public CipherInputStream acquire(InputStream i) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException{
        
        Cipher cipher = getCipher();
        
        cipher.init(
            Cipher.DECRYPT_MODE, 
            keySpec, 
            initVectorSpec
            );
        
        return new CipherInputStream(i,cipher);
    }
    
    //monolithic cipher API
    
    public byte[] encrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException{
        Cipher cipher = getCipher();
        cipher.init(
            Cipher.ENCRYPT_MODE, 
            keySpec, 
            initVectorSpec
            );
        
        return cipher.doFinal(data);
    }
    
    public byte[] decrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException{
        Cipher cipher = getCipher();
        cipher.init(
            Cipher.DECRYPT_MODE, 
            keySpec, 
            initVectorSpec
            );
        
        return cipher.doFinal(data);
    }

}
