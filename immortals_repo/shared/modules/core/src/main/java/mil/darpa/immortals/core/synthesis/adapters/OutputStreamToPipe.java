package mil.darpa.immortals.core.synthesis.adapters;

import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Allows simple function-based implementation of a single part of a pipeline
 * Created by awellman@bbn.com on 6/21/16.
 */
public class OutputStreamToPipe<OutputStreamType> extends OutputStream implements ConsumingPipe<byte[]> {
    private final OutputStream outputStream;

    public OutputStreamToPipe(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public final synchronized void consume(byte[] input) {
        try {
            outputStream.write(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
    public void flushPipe() {
        try {
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void closePipe() {
        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
