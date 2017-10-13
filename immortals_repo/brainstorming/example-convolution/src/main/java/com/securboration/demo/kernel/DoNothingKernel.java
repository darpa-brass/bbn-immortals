package com.securboration.demo.kernel;

import java.awt.image.BufferedImage;

public class DoNothingKernel implements IImageProcessingKernel {
    @Override
    public BufferedImage process(BufferedImage input) {
        try {
            Thread.sleep(1000l);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return input;
    }

}
