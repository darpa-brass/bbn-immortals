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
    private IvParameterSpec initVectorSpec;

    public CipherImplJavaxCrypto(){}
    
    protected Provider getProvider(){
        return null;
    }

    public static CipherImplJavaxCrypto initCipherImpl() throws Exception {
        CipherImplJavaxCrypto cipherImpl = new CipherImplJavaxCrypto();
        cipherImpl.configure("AES", 16, "CBC", "PKCS5Padding", "a test password", "an init vector");
        return cipherImpl;
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

//    @Override
//    public CipherInputStream acquire(InputStream i) {
//
//        try{
//            Cipher cipher = getCipher();
//    
//            cipher.init(
//                    Cipher.DECRYPT_MODE,
//                    keySpec,
//                    initVectorSpec
//            );
//    
//            return new CipherInputStream(i,cipher);
//        } catch(Exception e){
//            throw new RuntimeException(e);
//        }
//    }
    
    @Override
    public CipherInputStream acquire(final InputStream in) {

        try{
            final Cipher cipher = getCipher();
    
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    keySpec,
                    initVectorSpec
            );
    
            return new CipherInputStream(in,cipher)
//            {
//
//                private void $(String format, Object...args){
//                    System.out.printf(
//                        "  $[thread %s] : %s", 
//                        Thread.currentThread().getName(),
//                        String.format(format, args)
//                        );
//                }
//                
//                @Override
//                public int available() throws IOException {
//                    final int a = super.available();
//                    
//                    $("available() about to return %d\n",a);
//                    
//                    return a;
//                }
//
//                @Override
//                public void close() throws IOException {
//                    $("close() about to be called\n");
//                    super.close();
//                    $("close() called\n");
//                }
//
//                @Override
//                public boolean markSupported() {
//                    final boolean m = super.markSupported();
//                    $("markSupported() about to return %s\n",m);
//                    return m;
//                }
//
//                @Override
//                public int read() throws IOException {
//                    final int read = super.read();
//                    $("read() about to return %d\n",read);
//                    return read;
//                }
//
//                @Override
//                public int read(byte[] b, int off, int len) throws IOException {
//                    final int read = super.read(b, off, len);
//                    
//                    $("read([%d],off=%d,len=%d) about to return %d\n",b.length,off,len,read);
//                    
//                    return read;
//                }
//
//                @Override
//                public int read(byte[] b) throws IOException {
//                    final int read = super.read(b);
//                    
//                    $("read([%d]) about to return %d\n",b.length,read);
//                    
//                    return read;
//                }
//
//                @Override
//                public long skip(long n) throws IOException {
//                    final long skip = skip(n);
//                    
//                    $("skip(%d) about to return %d\n",n,skip);
//                    
//                    return skip;
//                }
//
//                @Override
//                public synchronized void mark(int readlimit) {
//                    super.mark(readlimit);
//                    
//                    $("mark(%d) about to return\n",readlimit);
//                }
//
//                @Override
//                public synchronized void reset() throws IOException {
//                    super.reset();
//                    
//                    $("reset() about to return\n");
//                }
//                
//            }
//            
//            {
//                
//                private boolean eosMode = false;
//                private byte[] lastChunk = null;
//                private boolean noMoreData = false;
//
//                @Override
//                public int available() throws IOException {
//                    return in.available();
//                }
//
//                @Override
//                public void close() throws IOException {
//                    try {
//                        lastChunk = cipher.doFinal();
//                    } catch (IllegalBlockSizeException
//                            | BadPaddingException e) {
//                        throw new RuntimeException(e);
//                    }
//                    eosMode = true;
//                    in.close();
//                }
//
//                @Override
//                public boolean markSupported() {
//                    return in.markSupported();
//                }
//
//                @Override
//                public int read() throws IOException {
//                    return in.read();
//                }
//
//                @Override
//                public int read(byte[] arg0, int arg1, int arg2)
//                        throws IOException {
//                    return in.read(arg0, arg1, arg2);
//                }
//
//                @Override
//                public int read(byte[] arg0) throws IOException {
//                    return in.read(arg0);
//                }
//
//                @Override
//                public long skip(long arg0) throws IOException {
//                    return in.skip(arg0);
//                }
//                
//            }
            ;
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


