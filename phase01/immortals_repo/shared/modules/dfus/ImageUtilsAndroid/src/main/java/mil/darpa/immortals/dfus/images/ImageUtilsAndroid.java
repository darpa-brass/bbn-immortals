package mil.darpa.immortals.dfus.images;

import android.graphics.Bitmap;

/**
 * Created by awellman@bbn.com on 2/23/16.
 * <p>
 * Image utility DFUs
 * <p>
 */

public class ImageUtilsAndroid {
    public static Bitmap resizeBitmap(Bitmap bmp, double maxMegapixels) {
        double height = bmp.getHeight();
        double width = bmp.getWidth();
        double targetPixels = maxMegapixels * 1000000;
        if ((height * width) > targetPixels) {
            double aspectRatio = height / width;

//            int newHeight = (int) Math.floor((height / width) * targetPixels);
//            int newWidth = (int) Math.floor((width / height) * targetPixels);

            int newHeight = (int) Math.floor(Math.sqrt(targetPixels * aspectRatio));
            int newWidth = (int) Math.floor(newHeight / aspectRatio);
            return Bitmap.createScaledBitmap(bmp, newWidth, newHeight, true);
        }
        return bmp;
    }
}
