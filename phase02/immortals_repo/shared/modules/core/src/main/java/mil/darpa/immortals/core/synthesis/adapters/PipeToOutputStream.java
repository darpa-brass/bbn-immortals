package mil.darpa.immortals.core.synthesis.adapters;

import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Allows simple function-based implementation of a single part of a pipeline
 * Created by awellman@bbn.com on 6/21/16.
 */
public class PipeToOutputStream extends OutputStream implements ConsumingPipe<byte[]> {
    private final ConsumingPipe nextObjectPipe;

    public PipeToOutputStream(ConsumingPipe<byte[]> next) {
        this.nextObjectPipe = next;
    }

    @Override
    public final void write(int b) throws IOException {
        write(new byte[]{(byte) b});
    }

    @Override
    public final synchronized void write(byte[] input) {
        nextObjectPipe.consume(input);
    }

    @Override
    public final void write(byte[] b, int off, int len) throws IOException {
        write(Arrays.copyOfRange(b, off, len));
    }

    @Override
    public void flush() throws IOException {
        nextObjectPipe.flushPipe();
    }

    @Override
    public void close() throws IOException {
        nextObjectPipe.closePipe();
    }

    @Override
    public void consume(byte[] input) {
        nextObjectPipe.consume(input);
    }

    @Override
    public void flushPipe() {
        nextObjectPipe.flushPipe();
    }

    @Override
    public void closePipe() {
        nextObjectPipe.closePipe();
    }
}
