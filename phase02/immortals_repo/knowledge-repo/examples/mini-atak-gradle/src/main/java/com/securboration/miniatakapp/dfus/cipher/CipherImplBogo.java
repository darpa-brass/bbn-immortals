package com.securboration.miniatakapp.dfus.cipher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherDecrypt;
import com.securboration.immortals.ontology.functionality.alg.encryption.AspectCipherEncrypt;

import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;

/**
 * A simple BogoCipher. It "encrypts" binary data by bitwise NOTing each byte.
 * Decryption is therefore the same operation as encryption.
 * 
 * @author jstaples
 */
@DfuAnnotation(
    functionalityBeingPerformed = com.securboration.immortals.ontology.functionality.alg.encryption.Cipher.class
    )
public class CipherImplBogo implements CipherImplApi{

    public CipherImplBogo() {/* empty */}
    
    private static int notIntAsByte(int data){
        return ~data;
    }
    
    private static byte notByte(byte data){
        return (byte)~data;
    }
    
    private static void notByteArrayInSitu(byte[] data){
        for(int i=0;i<data.length;i++){
            data[i] = notByte(data[i]);
        }
    }
    
    private static byte[] copyOf(byte[] data, int off, int len){
        final byte[] copy = new byte[len];
        
        for(int i=0;i<len;i++){
            copy[i] = data[i+off];
        }
        
        return copy;
    }
    
    @Override
    public OutputStream acquire(final OutputStream o) {
        return new OutputStream(){

            @Override
            public void write(
                    byte[] data, 
                    int off, 
                    int len
                    ) throws IOException {
                
                data = copyOf(data,off,len);
                
                for(int i=off;i<len;i++){
                    data[i] = notByte(data[i]);
                }
                
                o.write(data,off,len);
            }

            @Override
            public void write(byte[] arg0) throws IOException {
                arg0 = copyOf(arg0,0,arg0.length);
                
                notByteArrayInSitu(arg0);
                
                o.write(arg0);
            }

            @Override
            public void write(int arg0) throws IOException {
                o.write(notIntAsByte(arg0));
            }

            @Override
            public void close() throws IOException {
                o.close();
            }

            @Override
            public void flush() throws IOException {
                o.flush();
            }
            
        };
    }

    @Override
    public InputStream acquire(final InputStream i) {

        return new InputStream(){
            
            @Override
            public int read() throws IOException {
                return notIntAsByte(i.read());
            }
            
            @Override
            public int read(byte[] b) throws IOException {
                final int bytesRead = i.read(b);
                for(int i=0;i<bytesRead;i++){
                    b[i]=notByte(b[i]);
                }
                
                return bytesRead;
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                final int bytesRead = i.read(b,off,len);
                
                for(int i=off;i<len;i++){
                    b[i]=notByte(b[i]);
                }
                
                return bytesRead;
            }
            
        };
    }

    @Override
    public byte[] encrypt(byte[] data) {
        notByteArrayInSitu(data);
        return data;
    }

    @Override
    @FunctionalAspectAnnotation(aspect = AspectCipherEncrypt.class)
    public byte[] encryptChunk(byte[] data) {
        notByteArrayInSitu(data);
        return data;
    }

    @Override
    @FunctionalAspectAnnotation(aspect = AspectCipherDecrypt.class)
    public byte[] decryptChunk(byte[] data) {
        notByteArrayInSitu(data);
        return data;
    }
    
    @Override
    public byte[] encryptFinish() {
        return new byte[]{};
    }

    @Override
    public byte[] decryptFinish() {
        return new byte[]{};
    }

    @Override
    public byte[] decrypt(byte[] data) {
        notByteArrayInSitu(data);
        return data;
    }

    @Override
    public void configure(
            String algorithm, 
            int keyLengthBytes,
            String chainingMode, 
            String paddingScheme, 
            String keyPhrase,
            String initVectorPhrase
            ) {/*empty*/}

}


