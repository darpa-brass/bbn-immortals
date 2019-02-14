package com.securboration.miniatakapp.dfus.cipher;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that delegates calls to its write methods in a manner that
 * ensures that the length of any data written to its delegate stream is an
 * exact multiple of the provided block size in bytes
 * 
 * @author jstaples
 *
 */
public class DataPaddingOutputStream extends OutputStream {
    
    private final int blockSizeBytes;
    private final OutputStream delegate;
    
    public DataPaddingOutputStream(
            final int blockSizeBytes, 
            final OutputStream delegate
            ) {
        super();
        this.blockSizeBytes = blockSizeBytes;
        this.delegate = delegate;
    }

    @Override
    public void write(int b) throws IOException {
        //Warning: wrapping individual bytes is *hugely* wasteful 
        
        final byte[] converted = 
                Blockifier.blockify(new byte[]{(byte)b},blockSizeBytes);
        delegate.write(converted);
    }

    @Override
    public void write(byte[] b) throws IOException {
        final byte[] converted = 
                Blockifier.blockify(b, blockSizeBytes);
        delegate.write(converted);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        final byte[] converted = 
                Blockifier.blockify(b, off,len, blockSizeBytes);
        delegate.write(converted);
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
    
    

}
