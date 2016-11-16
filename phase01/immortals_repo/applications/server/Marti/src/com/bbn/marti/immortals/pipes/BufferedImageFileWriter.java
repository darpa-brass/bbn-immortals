package com.bbn.marti.immortals.pipes;

import mil.darpa.immortals.core.synthesis.interfaces.ConsumingPipe;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * This is really just a simple means of validating image processing.
 * Created by awellman@bbn.com on 1/28/16.
 */
public class BufferedImageFileWriter implements ConsumingPipe<BufferedImage> {

    public static final String IMAGE_FORMAT = "jpg";


    // The current save "index"
    private static int identifier = 0;

    // How many photos to save until it starts overwriting
    private static int maxSaves = 10;

    private synchronized String getPhotoFileName() {
        String imageName = "image" + identifier + "." + IMAGE_FORMAT;

        if (identifier >= maxSaves) {
            identifier = 0;
        } else {
            identifier++;
        }
        return imageName;
    }

    @Override
    public void consume(BufferedImage bufferedImage) {
        try {
            ImageIO.write(bufferedImage, IMAGE_FORMAT, new File(getPhotoFileName()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void flushPipe() {

    }

    @Override
    public void closePipe() {

    }
}
