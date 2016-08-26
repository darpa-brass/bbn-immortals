package com.securboration.demo.convolvers;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * Naive convolution implementation
 * 
 * @author jstaples
 *
 */
public class NaiveConvolver implements IConvolver {

    private static float[][][] convertImageToData(BufferedImage buf) {
        Raster raster = buf.getData();

        float[][][] data = new float[raster.getNumBands()][raster
                .getWidth()][raster.getHeight()];

        for (int band = 0; band < raster.getNumBands(); band++) {
            for (int row = 0; row < raster.getWidth(); row++) {
                for (int col = 0; col < raster.getHeight(); col++) {
                    float value = raster.getSampleFloat(row, col, band);
                    data[band][row][col] = value;
                }
            }
        }

        return data;
    }

    private static BufferedImage convertDataToImage(float[][][] data) {
        final int bands = data.length;
        final int rows = data[0].length;
        final int cols = data[0][0].length;
        BufferedImage image = new BufferedImage(rows, cols,
                BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();

        for (int band = 0; band < bands; band++) {
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    raster.setSample(row, col, band, data[band][row][col]);
                }
            }
        }

        return image;
    }

    private static float[][] convolve(final float[][] band,
            final float[][] kernel) {
        final int kernelDim = kernel.length;
        final int kernelRadius = kernelDim / 2;

        final int rows = band.length;
        final int cols = band[0].length;

        float[][] result = new float[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // process pixel i,j

                final int minRow = i - kernelRadius;
                final int maxRow = i + kernelRadius;
                final int minCol = j - kernelRadius;
                final int maxCol = j + kernelRadius;

                float convolvedValue = 0;

                for (int p = minRow, kernelRow = 0; p <= maxRow; p++, kernelRow++) {
                    for (int q = minCol, kernelCol = 0; q <= maxCol; q++, kernelCol++) {
                        boolean inRange = true;

                        if (p < 0) {
                            inRange = false;
                        }

                        if (p >= rows) {
                            inRange = false;
                        }

                        if (q < 0) {
                            inRange = false;
                        }

                        if (q >= cols) {
                            inRange = false;
                        }

                        if (!inRange) {
                            // do nothing
                        } else {
                            // multiply the pixel at [i,j] with the appropriate
                            // mask pixel
                            convolvedValue += band[p][q]
                                    * kernel[kernelRow][kernelCol];
                        }
                    }
                }

                if (convolvedValue < 0) {
                    convolvedValue = 0;// TODO: signal clipping occurs here
                }
                result[i][j] = convolvedValue;
            }
        }

        return result;
    }

    private BufferedImage convolveImpl(BufferedImage original,
            final float[][] kernel) {

        final int N_CONVOLUTIONS = 1;

        BufferedImage currentImage = original;

        for (int i = 0; i < N_CONVOLUTIONS; i++) {
            float[][][] data = convertImageToData(currentImage);
            float[][][] processed = new float[data.length][][];

            int count = 0;
            for (float[][] dataBand : data) {
                float[][] band = convolve(dataBand, kernel);

                // TODO: signal clipping occurs here
                {
                    final float INTENSITY_LIMIT = 255;
                    for (int r = 0; r < band.length; r++) {
                        for (int c = 0; c < band[0].length; c++) {
                            if (band[r][c] > INTENSITY_LIMIT) {
                                band[r][c] = INTENSITY_LIMIT;
                            }

                            if (band[r][c] < 0f) {
                                band[r][c] = 0f;
                            }
                        }
                    }
                }

                processed[count] = band;
                count++;
            }

            currentImage = convertDataToImage(processed);
        }

        return currentImage;
    }

    public NaiveConvolver() {
        super();
    }

    @Override
    public BufferedImage convolve(BufferedImage input, float[][] kernel) {
        return convolveImpl(input, kernel);
    }

}
