package mil.darpa.immortals.core.synthesis.adapters;

import mil.darpa.immortals.core.synthesis.interfaces.ReadableObjectPipeInterface;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by awellman@bbn.com on 7/15/16.
 */
public class PipeToInputStream extends InputStream implements ReadableObjectPipeInterface<byte[]> {

    private byte[] singleByteBuf = new byte[1];

    byte[] buffer;
    int bufferIdx = 0;

    private final ReadableObjectPipeInterface<byte[]> objectPipe;

    public PipeToInputStream(ReadableObjectPipeInterface<byte[]> objectPipe) {
        this.objectPipe = objectPipe;
    }

    @Override
    public synchronized int read() throws IOException {
        int readCount = read(singleByteBuf, 0, 1);
        if (readCount >= 0) {
            return singleByteBuf[0] & 0xff;
        } else {
            return -1;
        }
    }

    @Override
    public synchronized int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        if (buffer == null) {
            buffer = objectPipe.produce();
            bufferIdx = 0;
        }

        if (buffer == null) {
            return -1;
        }

        int readBytes;

        int bufferBytes = buffer.length - bufferIdx;

        if (len <= bufferBytes) {
            System.arraycopy(buffer, bufferIdx, b, off, len);
            bufferIdx += len;
            readBytes = len;
        } else {
            try {
                System.arraycopy(buffer, bufferIdx, b, off, bufferBytes);
                bufferIdx += bufferBytes;
                readBytes = bufferBytes;
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new RuntimeException(e);
            }
        }

        if (bufferIdx == buffer.length) {
            buffer = null;
            bufferIdx = 0;
        }

        return readBytes;
    }

    @Override
    public synchronized long skip(long n) throws IOException {
        throw new RuntimeException("Skip not yet supported!");
    }

    @Override
    public synchronized int available() throws IOException {
        if (buffer == null) {
            return 0;
        } else {
            return buffer.length - bufferIdx;
        }
    }

    @Override
    public byte[] produce() {
        return objectPipe.produce();
    }

    @Override
    public void close() throws IOException {
        objectPipe.closePipe();
    }

    @Override
    public synchronized void closePipe() {
        objectPipe.closePipe();
    }

    @Override
    public int getBufferSize() {
        return objectPipe.getBufferSize();
    }
}
