package com.securboration.demo.kernel;

import java.awt.image.BufferedImage;
import java.util.Random;

import com.securboration.demo.convolvers.AwtConvolver;
import com.securboration.demo.convolvers.IConvolver;
import com.securboration.demo.convolvers.NaiveConvolver;

/**
 * An enumeration of canned convolution problem instances. A problem instance
 * consists of various convolution implementations that operate on a kernel
 * 
 * @author Securboration
 *
 */
public enum ConvolutionConfigurations {
  convolution_gaussian_3x3(
      new float[][]{
          {1,2,1},
          {2,4,2},
          {1,2,1},
      }),
  convolution_gaussian_5x5(
      new float[][]{
          {2 ,4 ,5 ,4 ,2 },
          {4 ,9 ,12,9 ,4 },
          {5 ,12,15,12,5 },
          {4 ,9 ,12,9 ,4 },
          {2 ,4 ,5 ,4 ,2 },
      }),
  convolution_edge_detection_3x3(
      new float[][]{
          {-1,-1,-1},
          {-1,+8,-1},
          {-1,-1,-1},
      }),
  convolution_sharpen_3x3(
      new float[][]{
          {+1,+1,+1},
          {+1,-7,+1},
          {+1,+1,+1},
      }),
  convolution_emboss_3x3(
      new float[][]{
          {-1,-1,+0},
          {-1,+0,+1},
          {+0,+1,+1},
      }),
  convolution_random_15x15(
      generateFilter(15));
      
      ;
  
    private final IImageProcessingKernel naiveImpl;
    private final IImageProcessingKernel efficientImpl;

    private final float[][] kernel;

    private ConvolutionConfigurations(float[][] kernel) {
        kernel = normalize(kernel);
        this.kernel = kernel;

        this.naiveImpl = new ConvolutionProblemInstance(new NaiveConvolver(),
                kernel);

        this.efficientImpl = new ConvolutionProblemInstance(new AwtConvolver(),
                kernel);
    }

    private static class ConvolutionProblemInstance
            implements IImageProcessingKernel {
        private final float[][] kernel;
        private final IConvolver convolver;

        ConvolutionProblemInstance(IConvolver convolver, float[][] kernel) {
            this.kernel = kernel;
            this.convolver = convolver;
        }

        @Override
        public BufferedImage process(BufferedImage input) {
            return convolver.convolve(input, kernel);
        }

    }

    public IImageProcessingKernel getNaiveConvolver() {
        return naiveImpl;
    }

    public IImageProcessingKernel getEfficientConvolver() {
        return efficientImpl;
    }

    private static float[][] normalize(float[][] data) {
        float sum = 0;
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                sum += data[i][j];
            }
        }

        if (sum == 0) {
            return data;
        }

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                data[i][j] = data[i][j] / sum;
            }
        }

        return data;
    }

    private static float[][] generateFilter(int n) {
        if (n % 2 == 0) {
            throw new RuntimeException("n must be odd");
        }

        Random r = new Random(System.currentTimeMillis());
        float[][] filter = new float[n][n];

        for (int i = 0; i < n; i++) {
            float sum = 0;
            for (int j = 0; j < n; j++) {
                float gaussian = (float) r.nextGaussian();

                filter[i][j] = gaussian;
                sum += gaussian;
            }

            if (sum != 0) {
                // normalize
                for (int j = 0; j < n; j++) {
                    filter[i][j] = filter[i][j] / sum;
                }
            }
        }

        return filter;
    }

    public float[][] getKernel() {
        return kernel;
    }
  
}
