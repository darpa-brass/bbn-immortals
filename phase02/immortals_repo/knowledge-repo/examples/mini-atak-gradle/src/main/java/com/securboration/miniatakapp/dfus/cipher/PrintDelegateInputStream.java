package com.securboration.miniatakapp.dfus.cipher;

import java.io.IOException;
import java.io.InputStream;

public class PrintDelegateInputStream extends InputStream {
    
    private final String tag;
    private final InputStream delegate;
    
    public PrintDelegateInputStream(
            String tag,
            InputStream delegate
            ){
        this.tag = tag;
        this.delegate = delegate;
    }
    
    private void printf(String format, Object...args){
        System.out.printf(
            ">> [%s] [%s] -> [a %s] %s", 
            tag,
            Thread.currentThread().getName(),
            this.delegate.getClass().getName(),
            String.format(format, args)
            );
    }

    @Override
    public int read() throws IOException {
//        printf("about to call read()");
        
        int valueRead = delegate.read();
        
        printf("read() returned value %d\n",valueRead);
        
        return valueRead;
    }

    @Override
    public int read(byte[] b) throws IOException {
//        printf("about to call read(byte[%d])",b.length);
        
        final int bytesRead = delegate.read(b);
        
        printf(
            "read(byte[%d]) returned value %d %s\n",
            b.length,
            bytesRead,
            bytesRead > 0 ? Blockifier.hex(b, 0, bytesRead) : "[none]"
            );
        
        return bytesRead;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
//        printf("about to call read(byte[%d],%d,%d)",b.length,off,len);
        
        final int bytesRead = delegate.read(b);
        
        printf(
            "read(byte[%d],%d,%d) returned value %d %s\n",
            b.length,off,len,
            bytesRead,
            bytesRead > -1 ? Blockifier.hex(b, 0, bytesRead) : "[EOS]"
            );
        
        return bytesRead;
    }

    @Override
    public long skip(long n) throws IOException {
        printf("about to call skip(%d)\n",n);
        return delegate.skip(n);
    }

    @Override
    public int available() throws IOException {
        printf("about to call available()\n");
        return delegate.available();
    }

    @Override
    public void close() throws IOException {
        printf("about to call close()\n");
        delegate.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        printf("about to call mark(%d)\n",readlimit);
        delegate.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        printf("about to call reset()\n");
        delegate.reset();
    }

    @Override
    public boolean markSupported() {
        printf("about to call markSupported()\n");
        return delegate.markSupported();
    }

}
