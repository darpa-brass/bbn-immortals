package mil.darpa.immortals.dfus.images;

import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import java.awt.image.BufferedImage;

/**
 * Created by awellman@bbn.com on 6/22/16.
 */
public class BitmapDownsizer implements ConsumingPipe<BufferedImage> {

    private final double targetMegapixels;
    private final ConsumingPipe<BufferedImage> next;

    public BitmapDownsizer(double targetMegapixels, ConsumingPipe<BufferedImage> next) {
        this.targetMegapixels = targetMegapixels;
        this.next = next;
    }

    @Override
    public void consume(BufferedImage input) {
        BufferedImage output = ImageUtilsJava.resizeBitmap(input, targetMegapixels);
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
