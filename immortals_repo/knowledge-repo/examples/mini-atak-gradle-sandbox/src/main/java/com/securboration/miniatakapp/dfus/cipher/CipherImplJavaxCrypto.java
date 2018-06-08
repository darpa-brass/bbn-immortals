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

import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherDecrypt;
import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherEncrypt;

import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;

/**
 * A simple Cipher implementation that internally uses javax.crypto
 */
@DfuAnnotation(functionalityBeingPerformed = com.securboration.immortals.ontology.functionality.alg.encryption.Cipher.class)
public class CipherImplJavaxCrypto implements CipherImplApi{

    private Cipher encryptionCipher;
    private Cipher decryptionCipher;

    private String algorithm;
    private String chainingMode;
    private String paddingScheme;

    private SecretKeySpec keySpec;
    
    private String ivGeneratorString;

    public CipherImplJavaxCrypto(){}
    
    protected Provider getProvider(){
        return null;
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

    //streaming cipher API
    
    @Override
    public CipherOutputStream acquire(OutputStream o) {

        try{
            Cipher cipher = getCipher();
            final IvParameterSpec iv = getIv(cipher);
            
            if(iv != null){
                cipher.init(
                    Cipher.ENCRYPT_MODE,
                    keySpec,
                    iv
                    );
            } else {
                cipher.init(
                        Cipher.ENCRYPT_MODE,
                        keySpec
                );
            }
            
            return new CipherOutputStream(o,cipher);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    
    private IvParameterSpec getIv(Cipher cipher) throws NoSuchAlgorithmException{
        if(ivGeneratorString == null){
            return null;
        }
        
        return new IvParameterSpec(
            hash(ivGeneratorString,cipher.getBlockSize())
            );
    }
    
    @Override
    public CipherInputStream acquire(final InputStream in) {

        try{
            final Cipher cipher = getCipher();
            final IvParameterSpec iv = getIv(cipher);
    
            if(iv != null){
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    keySpec,
                    iv
                    );
            } else {
                cipher.init(
                        Cipher.DECRYPT_MODE,
                        keySpec
                );
            }
    
            return new CipherInputStream(in,cipher);
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    //monolithic cipher API

    @Override
    public byte[] encrypt(byte[] data) {
        throw new RuntimeException("no impl");
    }

    @Override
    @FunctionalAspectAnnotation(aspect = AspectCipherEncrypt.class)
    public byte[] encryptChunk(byte[] data) {
        throw new RuntimeException("no impl");
    }

    @Override
    @FunctionalAspectAnnotation(aspect = AspectCipherDecrypt.class)
    public byte[] decryptChunk(byte[] data) {
        throw new RuntimeException("no impl");
    }

    
    @Override
    public byte[] encryptFinish() {
        try{
            Cipher cipher;
            
            if(this.encryptionCipher != null) {
                cipher = this.encryptionCipher;
            } else {
                cipher = getCipher();
                
                final IvParameterSpec iv = getIv(cipher);
                
                if(iv != null){
                    cipher.init(
                        Cipher.ENCRYPT_MODE,
                        keySpec,
                        iv
                        );
                } else {
                    cipher.init(
                        Cipher.ENCRYPT_MODE, 
                        keySpec
                        );
                }
    
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
        throw new RuntimeException("no impl");
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
            
            this.ivGeneratorString = initVectorPhrase;
        } catch(Exception e){
            throw new RuntimeException(e);
        }
    }

}


