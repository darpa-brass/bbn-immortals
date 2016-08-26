package mil.darpa.immortals.dfus.images;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.securboration.immortals.ontology.functionality.imageprocessor.AspectImageProcessorProcessImage;
import com.securboration.immortals.ontology.functionality.imageprocessor.ImageProcessor;
import com.securboration.immortals.ontology.resources.DiskResource;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;
import mil.darpa.immortals.core.synthesis.ObjectPipe;
import mil.darpa.immortals.core.synthesis.interfaces.ReadableObjectPipeInterface;
import mil.darpa.immortals.core.synthesis.interfaces.WriteableObjectPipeInterface;

/**
 * Created by awellman@bbn.com on 6/22/16.
 */
@DfuAnnotation(
        functionalityBeingPerformed = ImageProcessor.class,
        resourceDependencies = {
                DiskResource.class
        }
)
public class BitmapReader extends ObjectPipe<String, Bitmap> {

    @FunctionalAspectAnnotation(
            aspect = AspectImageProcessorProcessImage.class
    )
    public BitmapReader(WriteableObjectPipeInterface<Bitmap> next) {
        super(next);
    }

    public BitmapReader(ReadableObjectPipeInterface<String> previous) {
        super(previous);
    }

    @Override
    protected Bitmap process(String imageLocation) {
        return BitmapFactory.decodeFile(imageLocation);
    }

    @Override
    protected Bitmap flushToOutput() {
        return null;
    }

    @Override
    protected void preNextClose() {

    }

    @Override
    public int getBufferSize() {
        throw new RuntimeException("Not supported!");
    }
}
