package mil.darpa.immortals.core.synthesis.adapters;

import mil.darpa.immortals.core.synthesis.interfaces.ReadableObjectPipeInterface;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Allows simple function-based implementation of a single part of a pipeline
 * Created by awellman@bbn.com on 6/21/16.
 */
public class InputStreamToPipe extends InputStream implements ReadableObjectPipeInterface<byte[]> {
    public static final int DEFAULT_BUFFER_SIZE =  16384;

    private final int bufferSize;
    private final InputStream source;
    private int readCount = 0;
    private final byte[] buffer;

    public InputStreamToPipe(InputStream source, int bufferSize) {
        this.source = source;
        this.bufferSize = bufferSize;
        this.buffer = new byte[bufferSize];

    }

    @Override
    public byte[] produce() {
        try {
            readCount = source.read(buffer);
            if (readCount >= 0) {
                return Arrays.copyOf(buffer, readCount);
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int read() throws IOException {
        return source.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return source.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return source.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return source.skip(n);
    }

    @Override
    public int available() throws IOException {
        return source.available();
    }

    @Override
    public void close() throws IOException {
        source.close();
    }

    @Override
    public void closePipe() {
        try {
            source.close();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public synchronized void mark(int readlimit) {
        source.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        source.reset();
    }

    @Override
    public boolean markSupported() {
        return source.markSupported();
    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }
}
