package mil.darpa.immortals.core.synthesis.adapters;

import mil.darpa.immortals.core.synthesis.interfaces.ProducingPipe;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by awellman@bbn.com on 7/19/16.
 */
public class InputStreamPipe extends InputStream implements ProducingPipe<byte[]> {
    private PipeToInputStream pipeToInputStream;
    private InputStreamToPipe inputStreamToPipe;
    private InputStream inputStream;
    private ProducingPipe<byte[]> pipe;

    public InputStreamPipe() {
    }


    protected void delayedInit(InputStream input, int bufferSize) {
        inputStreamToPipe = new InputStreamToPipe(input, bufferSize);
        inputStream = inputStreamToPipe;
        pipe = inputStreamToPipe;
        pipeToInputStream = null;
    }

    protected void delayedInit(ProducingPipe<byte[]> input) {
        inputStreamToPipe = null;
        pipeToInputStream = new PipeToInputStream(input);
        inputStream = pipeToInputStream;
        pipe = pipeToInputStream;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        super.mark(readlimit);
    }


    @Override
    public byte[] produce() {
        return pipe.produce();
    }

    @Override
    public void closePipe() {
        pipe.closePipe();
    }

    @Override
    public int getBufferSize() {
        return pipe.getBufferSize();
    }
}
