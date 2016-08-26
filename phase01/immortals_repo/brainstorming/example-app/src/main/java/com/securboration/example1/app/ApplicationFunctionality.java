package com.securboration.example1.app;

import com.securboration.example.annotations.SemanticTypeBinding;

/**
 * <b>Example 1</b>
 * <p>
 * Illustrates using a single DFU for convolution with many possible
 * implementations
 * <p>
 * 
 * A simple example application that distills the DFU problem to an approachable
 * degree.
 * <p>
 * The application's only functionality is to sharpen an image, which is
 * achieved using convolution.
 * <p>
 * A key point in understanding the role of DFUs and IMMoRTALS in the context of
 * this application is that there are <b>many</b> ways to convolve an image,
 * each having a set of quantifiable performance and resource tradeoffs.
 * <p>
 * Specifically, the following convolution implementations are available:
 * 
 * <table border="1">
 * <tr>
 * <td><b>impl</b></td>
 * <td><b>performance</b></td>
 * <td><b>library requirements</b></td>
 * <td><b>additional device requirements</b></td>
 * </tr>
 * 
 * <tr>
 * <td>in-house CPU impl</td>
 * <td>1/10</td>
 * <td>none</td>
 * <td>none</td>
 * </tr>
 * 
 * <tr>
 * <td>in-house network impl</td>
 * <td>*network-dependent</td>
 * <td>none</td>
 * <td>network</td>
 * </tr>
 * 
 * <tr>
 * <td>commons-math3</td>
 * <td>5/10</td>
 * <td>requires commons-math3</td>
 * <td>none</td>
 * </tr>
 * 
 * <tr>
 * <td>native impl</td>
 * <td>10/10</td>
 * <td>in addition to the lib itself, requires using the correct native libs
 * compiled for this device</td>
 * <td>requires GPU</td>
 * </tr>
 * 
 * </table>
 * 
 * 
 * @author jstaples
 *
 */
public class ApplicationFunctionality {

    // an instantiation of a DFU
    private NaiveConvolver convolver = new NaiveConvolver();

    @SemanticTypeBinding(semanticType = "file://ontology.immortals.securboration.com/r1.0/Datatypes.owl#TwoDimensionalSquareArray")
    private final double[][] SHARPEN_KERNEL = new double[][] { { -1, -1, -1 }, { -1, +12, -1 }, { -1, -1, -1 } };

    /**
     * 
     * @param i
     *            an image to sharpen
     * @return the sharpened image
     */
    public Image sharpen(Image i) {

        ConvolutionKernel kernel = ConvolutionKernel.create(SHARPEN_KERNEL);

        // invoke DFU functionality
        Image convolved = convolver.convolve(i, kernel);

        return convolved;
    }

}
