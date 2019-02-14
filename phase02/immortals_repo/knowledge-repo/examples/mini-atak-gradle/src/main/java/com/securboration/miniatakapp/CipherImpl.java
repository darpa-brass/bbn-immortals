package com.securboration.miniatakapp;

import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherDecrypt;
import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherEncrypt;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;

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
@DfuAnnotation(functionalityBeingPerformed = com.securboration.immortals.ontology.functionality.alg.encryption.Cipher.class)
public class CipherImpl{

    private Cipher encryptionCipher;
    private Cipher decryptionCipher;

    private final String algorithm;
    private final String chainingMode;
    private final String paddingScheme;

    private final SecretKeySpec keySpec;
    private final IvParameterSpec initVectorSpec;

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
        if (initVectorPhrase != null) {
            this.initVectorSpec = getInitVectorSpec(initVectorPhrase, keyLengthBytes);
        } else {
            this.initVectorSpec = null;
        }
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
                new byte[8]
        );
    }

    //streaming cipher API

    public CipherOutputStream acquire(OutputStream o) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException{

        Cipher cipher = getCipher();

        if (initVectorSpec == null) {
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    keySpec
            );
        } else {
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    keySpec,
                    initVectorSpec
            );
        }

        return new CipherOutputStream(o,cipher);
    }

    public CipherInputStream acquire(InputStream i) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException{

        Cipher cipher = getCipher();

        if (initVectorSpec == null) {
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    keySpec
            );
        } else {
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    keySpec,
                    initVectorSpec
            );
        }

        return new CipherInputStream(i,cipher);
    }

    //monolithic cipher API

    public byte[] encrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException{
        Cipher cipher = getCipher();
        if (initVectorSpec == null) {
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    keySpec
            );
        } else {
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    keySpec,
                    initVectorSpec
            );
        }

        return cipher.doFinal(data);
    }

    @FunctionalAspectAnnotation(aspect = AspectCipherEncrypt.class)
    public byte[] encryptChunk(byte[] data) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException{
        Cipher cipher;

        if(this.encryptionCipher != null) {
            cipher = this.encryptionCipher;
        } else {
            cipher = getCipher();
            if (initVectorSpec == null) {
                cipher.init(
                        Cipher.DECRYPT_MODE,
                        keySpec
                );
            } else {
                cipher.init(
                        Cipher.DECRYPT_MODE,
                        keySpec,
                        initVectorSpec
                );
            }

            this.encryptionCipher = cipher;
        }

        return cipher.update(data);
    }

    @FunctionalAspectAnnotation(aspect = AspectCipherDecrypt.class)
    public byte[] decryptChunk(byte[] data) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException{
        Cipher cipher;

        if(this.decryptionCipher != null) {
            cipher = this.decryptionCipher;
        } else {
            cipher = getCipher();
            if (initVectorSpec == null) {
                cipher.init(
                        Cipher.DECRYPT_MODE,
                        keySpec
                );
            } else {
                cipher.init(
                        Cipher.DECRYPT_MODE,
                        keySpec,
                        initVectorSpec
                );
            }

            this.decryptionCipher = cipher;
        }

        return cipher.update(data);
    }

    
    public byte[] encryptFinish() throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher;
        
        if(this.encryptionCipher != null) {
            cipher = this.encryptionCipher;
        } else {
            cipher = getCipher();
            if (initVectorSpec == null) {
                cipher.init(
                        Cipher.DECRYPT_MODE,
                        keySpec
                );
            } else {
                cipher.init(
                        Cipher.DECRYPT_MODE,
                        keySpec,
                        initVectorSpec
                );
            }

            this.encryptionCipher = cipher;
        }
        
        return encryptionCipher.doFinal();
    }

    public byte[] decryptFinish() throws IllegalBlockSizeException, BadPaddingException {
        return decryptionCipher.doFinal();
    }

    public byte[] decrypt(byte[] data) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException{
        Cipher cipher = getCipher();
        
        if (initVectorSpec == null) {
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    keySpec
            );
        } else {
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    keySpec,
                    initVectorSpec
            );
        }
        byte[] result = cipher.doFinal(data);

        {//TODO: this feels janky
            byte[] trimmed = new byte[result.length - 16];
            System.arraycopy(result, 0, trimmed, 0, trimmed.length);
            result = trimmed;
        }

        return result;
    }

}


