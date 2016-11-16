package mil.darpa.immortals.dfus.images;

import android.graphics.Bitmap;
import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

/**
 * Created by awellman@bbn.com on 9/26/16.
 */
public class BitmapScaler implements ConsumingPipe<Bitmap> {

    private final ConsumingPipe<Bitmap> next;
    private final double scalingValue;

    public BitmapScaler(double scalingValue, ConsumingPipe<Bitmap> next) {
        this.next = next;
        this.scalingValue = scalingValue;
    }

    @Override
    public void consume(Bitmap input) {
        Bitmap output = ImageUtilsAndroid.scaleBitmap(input, scalingValue);
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
