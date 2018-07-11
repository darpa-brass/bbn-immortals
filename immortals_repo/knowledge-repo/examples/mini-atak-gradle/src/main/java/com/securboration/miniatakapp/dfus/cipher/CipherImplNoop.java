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
 */
@DfuAnnotation(functionalityBeingPerformed = com.securboration.immortals.ontology.functionality.alg.encryption.Cipher.class)
public class CipherImplNoop implements CipherImplApi{

    public CipherImplNoop() {/* empty */}
    
    @Override
    public OutputStream acquire(final OutputStream o) {
        return new OutputStream(){

            @Override
            public void write(
                    byte[] arg0, 
                    int arg1, 
                    int arg2
                    ) throws IOException {
                o.write(arg0,arg1,arg2);
            }

            @Override
            public void write(byte[] arg0) throws IOException {
                o.write(arg0);
            }

            @Override
            public void write(int arg0) throws IOException {
                o.write(arg0);
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
                return i.read();
            }

            @Override
            public int read(byte[] b) throws IOException {
                return i.read(b);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return i.read(b, off, len);
            }
            
        };
    }

    @Override
    public byte[] encrypt(byte[] data) {
        return data;
    }

    @Override
    @FunctionalAspectAnnotation(aspect = AspectCipherEncrypt.class)
    public byte[] encryptChunk(byte[] data) {
        return data;
    }

    @Override
    @FunctionalAspectAnnotation(aspect = AspectCipherDecrypt.class)
    public byte[] decryptChunk(byte[] data) {
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


