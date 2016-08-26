package mil.darpa.immortals.dfus.compression;

import mil.darpa.immortals.core.synthesis.adapters.InputStreamPipe;
import mil.darpa.immortals.core.synthesis.adapters.PipeToInputStream;
import mil.darpa.immortals.core.synthesis.interfaces.ReadableObjectPipeInterface;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by awellman@bbn.com on 7/6/16.
 */
public class GzipDecompressor extends InputStreamPipe {

    private InputStream decompressionStream;

    public GzipDecompressor(ReadableObjectPipeInterface<byte[]> producer) {
        this((InputStream) new PipeToInputStream(producer));
    }

    public GzipDecompressor(InputStreamPipe producer) {
        this((InputStream) producer);
    }

    public GzipDecompressor(InputStream producer) {
        try {
            GzipCompressionHelper gzipCompressionHelper = new GzipCompressionHelper();
            decompressionStream = gzipCompressionHelper.attachDecompressingInputStream(producer);
            delayedInit(decompressionStream, gzipCompressionHelper.getBufferSize());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
