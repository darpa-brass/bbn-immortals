package mil.darpa.immortals.dfus.compression;


import mil.darpa.immortals.core.synthesis.adapters.OutputStreamPipe;
import mil.darpa.immortals.core.synthesis.adapters.PipeToOutputStream;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by awellman@bbn.com on 7/6/16.
 */
public class GzipCompressor extends OutputStreamPipe {

    private final OutputStream compressionStream;

    public GzipCompressor(ConsumingPipe<byte[]> consumer) {
        this((OutputStream) new PipeToOutputStream(consumer));
    }

    public GzipCompressor(OutputStream outputStream) {
        try {
            GzipCompressionHelper gzipCompressionHelper = new GzipCompressionHelper();
            compressionStream = gzipCompressionHelper.attachCompressingOutputStream(outputStream);
            delayedInit(compressionStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public GzipCompressor(OutputStreamPipe consumer) {
        this((OutputStream) consumer);
    }
}
