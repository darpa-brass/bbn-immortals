package mil.darpa.immortals.dfus.images;

import mil.darpa.immortals.annotation.dsl.ontology.functionality.imagescaling.ImageScalingFactor;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import java.awt.image.BufferedImage;

/**
 * Created by awellman@bbn.com on 9/26/16.
 */
public class BitmapScaler implements ConsumingPipe<BufferedImage> {

    private final ConsumingPipe<BufferedImage> next;
    private final double scalingValue;

    public BitmapScaler(@ImageScalingFactor double scalingValue, ConsumingPipe<BufferedImage> next) {
        this.next = next;
        this.scalingValue = scalingValue;
    }

    @Override
    public void consume(BufferedImage input) {
        BufferedImage output = ImageUtilsJava.scaleBitmap(input, scalingValue);
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
