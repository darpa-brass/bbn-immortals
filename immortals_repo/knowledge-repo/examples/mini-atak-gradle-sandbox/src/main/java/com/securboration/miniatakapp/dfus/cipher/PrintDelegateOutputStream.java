package com.securboration.miniatakapp.dfus.cipher;

import java.io.IOException;
import java.io.OutputStream;

public class PrintDelegateOutputStream extends OutputStream {
    
    private final String tag;
    private final OutputStream delegate;
    
    public PrintDelegateOutputStream(
            String tag,
            OutputStream delegate
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
    public void write(int b) throws IOException {
        printf("write(%d) called\n",b);
        
        delegate.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b,0,b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        
        printf(
            "write(byte[],%d,%d) called with %s\n", 
            off,len,
            Blockifier.hex(b,off,len)
            );
        
        delegate.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        printf("flush() called\n");
        
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        printf("close() called\n");
    }

}
