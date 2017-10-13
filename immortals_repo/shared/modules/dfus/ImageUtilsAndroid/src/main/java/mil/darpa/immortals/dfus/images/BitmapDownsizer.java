package mil.darpa.immortals.dfus.images;

import android.graphics.Bitmap;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

/**
 * Created by awellman@bbn.com on 6/22/16.
 */
public class BitmapDownsizer implements ConsumingPipe<Bitmap> {

    private final double targetMegapixels;
    private final ConsumingPipe<Bitmap> next;

    public BitmapDownsizer(double targetMegapixels, ConsumingPipe<Bitmap> next) {
        this.targetMegapixels = targetMegapixels;
        this.next = next;
    }

    @Override
    public void consume(Bitmap input) {
        Bitmap output = ImageUtilsAndroid.resizeBitmap(input, targetMegapixels);
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
