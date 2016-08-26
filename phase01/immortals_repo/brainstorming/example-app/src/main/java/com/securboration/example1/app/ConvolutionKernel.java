package com.securboration.example1.app;

import com.securboration.example.annotations.SemanticTypeBinding;
import com.securboration.example.annotations.SemanticTypeConverter;

@SemanticTypeBinding(semanticType = "file://ontology.immortals.securboration.com/r1.0/Datatypes.owl#TwoDimensionalKernel")
public class ConvolutionKernel {

    @SemanticTypeConverter(inputSemanticType = "file://ontology.immortals.securboration.com/r1.0/Datatypes.owl#TwoDimensionalSquareArray", outputSemanticType = "file://ontology.immortals.securboration.com/r1.0/Datatypes.owl#TwoDimensionalKernel")
    public static ConvolutionKernel create(double[][] input) {
        return null;
    }

}
