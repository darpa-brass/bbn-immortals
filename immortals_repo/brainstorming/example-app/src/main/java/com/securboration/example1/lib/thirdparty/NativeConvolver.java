package com.securboration.example1.lib.thirdparty;

import com.securboration.example.annotations.Dfu;
import com.securboration.example.annotations.SemanticTypeBinding;

public class NativeConvolver {

    /**
     * Note that the following classes have annotations with identical semantic
     * URIs:
     * <ul>
     * <li>{@link com.securboration.example1.app.NaiveConvolver}</li>
     * <li>{@link com.securboration.example1.app.RemoteConvolver}</li>
     * <li>
     * {@link com.securboration.example1.lib.thirdparty.CommonsMathConvolver}
     * </li>
     * <li>{@link com.securboration.example1.lib.thirdparty.NativeConvolver}
     * </li>
     * </ul>
     * 
     * This tells us that although the interfaces look very different, their
     * functionality is the same.
     * 
     * @param i
     * @param kernel
     * @return
     */
    @Dfu(functionalityUri = "file://ontology.immortals.securboration.com/r1.0/Algorithms.owl#Convolve")
    public RgbImage convolve(
            @SemanticTypeBinding(semanticType = "file://ontology.immortals.securboration.com/r1.0/Datatypes.owl#Image") RgbImage i,
            @SemanticTypeBinding(semanticType = "file://ontology.immortals.securboration.com/r1.0/Datatypes.owl#TwoDimensionalKernel") float[][] kernel) {
        return null;
    }

}
