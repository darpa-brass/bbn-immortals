package mil.darpa.immortals.dfus;

import com.securboration.immortals.ontology.functionality.logger.Logger;
import com.securboration.immortals.ontology.resources.FileSystemResource;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.core.synthesis.adapters.OutputStreamPipe;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by awellman@bbn.com on 6/8/16.
 */
@DfuAnnotation(
        functionalityBeingPerformed = Logger.class,
        resourceDependencies = {
                FileSystemResource.class
        }
)
public class LatestSaFileByteWriter extends OutputStreamPipe {

    private final FileOutputStream fileOutputStream;

    public LatestSaFileByteWriter(final String filepath) {
        try {
            fileOutputStream = new FileOutputStream(filepath);
            super.delayedInit(fileOutputStream);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}