package mil.darpa.immortals.core.synthesis.adapters;

import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by awellman@bbn.com on 7/19/16.
 */
public class OutputStreamPipe extends OutputStream implements ConsumingPipe<byte[]> {
    private PipeToOutputStream pipeToOutputStream;
    private OutputStreamToPipe outputStreamToPipe;
    private OutputStream outputStream;
    private ConsumingPipe<byte[]> pipe;

    public OutputStreamPipe() {
    }

    protected void delayedInit(OutputStream output) {
        outputStreamToPipe = new OutputStreamToPipe(output);
        pipeToOutputStream = null;
        outputStream = outputStreamToPipe;
        pipe = outputStreamToPipe;
    }

    protected void delayedInit(ConsumingPipe<byte[]> output) {
        outputStreamToPipe = null;
        pipeToOutputStream = new PipeToOutputStream(output);
        outputStream = pipeToOutputStream;
        pipe = pipeToOutputStream;
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        outputStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }

    @Override
    public void consume(byte[] input) {
        pipe.consume(input);
    }

    @Override
    public void flushPipe() {
        pipe.flushPipe();
    }

    @Override
    public void closePipe() {
        pipe.closePipe();
    }
}
