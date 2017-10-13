package mil.darpa.immortals.dfus.images;


import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Java version of image scaling DFU
 * Created by awellman@bbn.com on 2/23/16.
 */

public class ImageUtilsJava {
    public static BufferedImage resizeBitmap(BufferedImage bmp, double maxMegapixels) {
        double height = bmp.getHeight();
        double width = bmp.getWidth();
        double targetPixels = maxMegapixels * 1000000;
        if ((height * width) > targetPixels) {
            double aspectRatio = height / width;

            int newHeight = (int) Math.floor(Math.sqrt(targetPixels * aspectRatio));
            int newWidth = (int) Math.floor(newHeight / aspectRatio);
            return resizeBitmap(bmp, newHeight, newWidth);
        }
        return bmp;
    }

    public static BufferedImage scaleBitmap(BufferedImage bmp, double scalingValue) {
        double height = bmp.getHeight();
        double width = bmp.getWidth();
        double totalPixels = height * width;
        double totalPixelsPrime = height * width * scalingValue;
        if (totalPixels > totalPixelsPrime) {
            double ratio = height / width;
            double widthPrime = Math.sqrt(totalPixelsPrime / ratio);
            double heightPrime = totalPixelsPrime / widthPrime;
            return resizeBitmap(bmp, (int) Math.floor(widthPrime), (int) Math.floor(heightPrime));
        }
        return bmp;
    }

    private static BufferedImage resizeBitmap(BufferedImage image, int newHeight, int newWidth) {
        Image i = image.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
        BufferedImage rval = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = rval.createGraphics();
        g.drawImage(i, 0, 0, null);
        g.dispose();
        return rval;
    }
}
