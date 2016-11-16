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

            int newHeight = (int) Math.floor(Math.sqrt(targetPixels * aspectRatio));
            int newWidth = (int) Math.floor(newHeight / aspectRatio);
            return Bitmap.createScaledBitmap(bmp, newWidth, newHeight, true);
        }
        return bmp;
    }

    public static Bitmap scaleBitmap(Bitmap bmp, double scalingValue) {
        double height = bmp.getHeight();
        double width = bmp.getWidth();
        double totalPixels = height * width;
        double totalPixelsPrime = height * width * scalingValue;
        if (totalPixels > totalPixelsPrime) {
            double ratio = height / width;
            double widthPrime = Math.sqrt(totalPixelsPrime / ratio);
            double heightPrime = totalPixelsPrime / widthPrime;
            return Bitmap.createScaledBitmap(bmp, (int) Math.floor(widthPrime), (int) Math.floor(heightPrime), true);
        }
        return bmp;
    }

    //    public static Bitmap scaleBitmap(Bitmap bmp, double scalingValue) {
//        int height = (int) Math.floor(((double) bmp.getHeight()) / scalingValue);
//        int width = (int) Math.floor(((double) bmp.getWidth()) / scalingValue);
//        return Bitmap.createScaledBitmap(bmp, width, height, true);
//    }
    public static void main(String[] args) {
    }
}
