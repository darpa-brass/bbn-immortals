package mil.darpa.immortals.dfus;

import com.securboration.immortals.ontology.functionality.logger.Logger;
import com.securboration.immortals.ontology.resources.FileSystemResource;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.core.synthesis.adapters.InputStreamPipe;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by awellman@bbn.com on 6/8/16.
 */
@DfuAnnotation(
        functionalityBeingPerformed = Logger.class,
        resourceDependencies = {
                FileSystemResource.class
        }
)
public class LatestSaFileByteReader extends InputStreamPipe {

    private final FileInputStream fileInputStream;

    private final int bufferSize = 4096;

    public LatestSaFileByteReader(final String filepath) {
        try {
            fileInputStream = new FileInputStream(filepath);
            super.delayedInit(fileInputStream, bufferSize);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int read() throws IOException {
        return super.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return super.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return super.read(b, off, len);
    }
}