package com.securboration.demo.convolvers;

import java.awt.image.BufferedImage;

/**
 * A simplistic interface describing a convolution operation applied to an image
 * 
 * @author jstaples
 *
 */
public interface IConvolver {
    /**
     * 
     * @param input
     *            the image to convolve
     * @param kernel
     *            the convolution kernel (a square 2d array)
     * @return the convolved image
     */
    public BufferedImage convolve(BufferedImage input, float[][] kernel);
}
