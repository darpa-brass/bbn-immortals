package com.securboration.miniatakapp.dfus.cipher;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherDecrypt;
import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherEncrypt;

import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;

/**
 * A simple Cipher implementation that internally uses javax.crypto
 */
@DfuAnnotation(functionalityBeingPerformed = com.securboration.immortals.ontology.functionality.alg.encryption.Cipher.class)
public class CipherImplBouncyCrypto implements CipherImplApi{

    private Cipher encryptionCipher;
    private Cipher decryptionCipher;

    private String algorithm;
    private String chainingMode;
    private String paddingScheme;

    private SecretKeySpec keySpec;
    private IvParameterSpec initVectorSpec;

    public CipherImplBouncyCrypto(){}
    
    protected Provider getProvider(){
        return new BouncyCastleProvider();
    }

    private Cipher getCipher() throws NoSuchAlgorithmException, NoSuchPaddingException{
        
        final String desc = algorithm + "/" + chainingMode + "/" + paddingScheme;
        
        if(getProvider() == null){
            return Cipher.getInstance(desc);
        }
        
        return Cipher.getInstance(
            desc,
            getProvider()
            );
    }

    private byte[] hash(
            String phrase,
            int lengthBytes
    ) throws NoSuchAlgorithmException{
        final Provider provider = getProvider();
        
        MessageDigest digest = 
                provider == null ? MessageDigest.getInstance("SHA-256") : MessageDigest.getInstance("SHA-256",provider);
        byte[] hash = digest.digest(phrase.getBytes());

        return Arrays.copyOf(hash, lengthBytes);
    }

    private SecretKeySpec getKeySpec(
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

    private IvParameterSpec getInitVectorSpec(
            final String initVectorPhrase,
            final int lengthBytes
    ) throws NoSuchAlgorithmException{
        return new IvParameterSpec(
            hash(initVectorPhrase,lengthBytes)
            );
        
    }

    //streaming cipher API

    @Override
    public CipherOutputStream acquire(OutputStream o) {

        try{
            Cipher cipher = getCipher();
    
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    keySpec,
                    initVectorSpec
            );
    
            return new CipherOutputStream(o,cipher);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public CipherInputStream acquire(InputStream i) {

        try{
            Cipher cipher = getCipher();
    
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    keySpec,
                    initVectorSpec
            );
    
            return new CipherInputStream(i,cipher);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    //monolithic cipher API

    @Override
    public byte[] encrypt(byte[] data) {
        try{
            Cipher cipher = getCipher();
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    keySpec,
                    initVectorSpec
            );
    
            return cipher.doFinal(data);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    @FunctionalAspectAnnotation(aspect = AspectCipherEncrypt.class)
    public byte[] encryptChunk(byte[] data) {
        try{
            Cipher cipher;
    
            if(this.encryptionCipher != null) {
                cipher = this.encryptionCipher;
            } else {
                cipher = getCipher();
                cipher.init(
                        Cipher.ENCRYPT_MODE,
                        keySpec,
                        initVectorSpec
                );
    
                this.encryptionCipher = cipher;
            }
    
            return cipher.update(data);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    @FunctionalAspectAnnotation(aspect = AspectCipherDecrypt.class)
    public byte[] decryptChunk(byte[] data) {
        try{
            Cipher cipher;
    
            if(this.decryptionCipher != null) {
                cipher = this.decryptionCipher;
            } else {
                cipher = getCipher();
                cipher.init(
                        Cipher.DECRYPT_MODE,
                        keySpec,
                        initVectorSpec
                );
    
                this.decryptionCipher = cipher;
            }
    
            return cipher.update(data);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    
    @Override
    public byte[] encryptFinish() {
        try{
            Cipher cipher;
            
            if(this.encryptionCipher != null) {
                cipher = this.encryptionCipher;
            } else {
                cipher = getCipher();
                cipher.init(
                        Cipher.ENCRYPT_MODE,
                        keySpec,
                        initVectorSpec
                );
    
                this.encryptionCipher = cipher;
            }
            
            return encryptionCipher.doFinal();
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decryptFinish() {
        try{
            return decryptionCipher.doFinal();
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decrypt(byte[] data) {
        
        try{
            Cipher cipher = getCipher();
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    keySpec,
                    initVectorSpec
            );
    
            byte[] result = cipher.doFinal(data);
    
            {//TODO: this feels janky
                byte[] trimmed = new byte[result.length - 16];
                System.arraycopy(result, 0, trimmed, 0, trimmed.length);
                result = trimmed;
            }
    
            return result;
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void configure(
            String algorithm, 
            int keyLengthBytes,
            String chainingMode, 
            String paddingScheme, 
            String keyPhrase,
            String initVectorPhrase
            ) {
        this.algorithm = algorithm;
        this.chainingMode = chainingMode;
        this.paddingScheme = paddingScheme;

        try{
            this.keySpec = getKeySpec(keyPhrase,algorithm,keyLengthBytes);
            this.initVectorSpec = getInitVectorSpec(initVectorPhrase,keyLengthBytes);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

}


