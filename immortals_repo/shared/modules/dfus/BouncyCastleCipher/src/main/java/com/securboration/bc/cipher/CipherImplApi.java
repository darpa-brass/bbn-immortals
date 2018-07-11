package com.securboration.bc.cipher;

import java.io.InputStream;
import java.io.OutputStream;

public interface CipherImplApi{

    public void configure(
            final String algorithm,
            final int keyLengthBytes,
            final String chainingMode,
            final String paddingScheme,

            final String keyPhrase,
            final String initVectorPhrase
    );

    public OutputStream acquire(OutputStream o);

    public InputStream acquire(InputStream i);

    public byte[] encrypt(byte[] data);

    public byte[] encryptChunk(byte[] data);

    public byte[] decryptChunk(byte[] data);

    public byte[] encryptFinish();

    public byte[] decryptFinish();

    public byte[] decrypt(byte[] data);

}

