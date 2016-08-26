package com.securboration.demo.convolvers;

import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
 * Third-party (awt) convolution implementation
 * 
 * @author jstaples
 *
 */
public class AwtConvolver implements IConvolver {

    @Override
    public BufferedImage convolve(BufferedImage input, float[][] kernel) {
        float[] kernel1d = new float[kernel.length * kernel.length];

        int index = 0;
        for (int i = 0; i < kernel.length; i++) {
            for (int j = 0; j < kernel.length; j++) {
                kernel1d[index] = kernel[i][j];
                index++;
            }
        }

        ConvolveOp convolver = new ConvolveOp(
                new Kernel(kernel.length, kernel.length, kernel1d));

        return convolver.filter(input, null);
    }

}
