package mil.darpa.immortals.dfus.images;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.securboration.immortals.ontology.functionality.imageprocessor.ImageProcessor;
import com.securboration.immortals.ontology.resources.DiskResource;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.Output;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

/**
 * Created by awellman@bbn.com on 6/22/16.
 */
@DfuAnnotation(
        functionalityBeingPerformed = ImageProcessor.class,
        resourceDependencies = {
                DiskResource.class
        }
)
public class BitmapReader implements ConsumingPipe<String> {

    private ConsumingPipe<Bitmap> next;

    public BitmapReader(@Output ConsumingPipe<Bitmap> next) {
        this.next = next;
    }

    @Override
    public void consume(String input) {
        Bitmap output = BitmapFactory.decodeFile(input);
        next.consume(output);
    }

    @Override
    public void flushPipe() {
        next.flushPipe();
    }

    @Override
    public void closePipe() {
        next.closePipe();
    }
}
