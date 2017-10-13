package com.securboration.immortals.test;

import com.securboration.immortals.ontology.core.TruthConstraint;
import com.securboration.immortals.ontology.functionality.dataproperties.ImageFidelityType;
import com.securboration.immortals.ontology.functionality.dataproperties.ImpactType;
import com.securboration.immortals.ontology.functionality.imageprocessor.AspectImageProcessorInitialize;
import com.securboration.immortals.ontology.functionality.imageprocessor.AspectImageProcessorProcessImage;
import com.securboration.immortals.ontology.functionality.imageprocessor.ImageProcessor;
import com.securboration.immortals.ontology.resources.compute.Cpu;
import com.securboration.immortals.ontology.resources.memory.PhysicalMemoryResource;

import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.compression.LossyTransformation;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.dataproperties.ImageFidelityImpact;

/**
 * An image processor DFU
 * 
 * It performs a lossy transformation on an input image to produce an output 
 * image with variably degraded fidelity in various dimensions
 * 
 * This is effectively the intersection of two DFUs, one that processes an image
 * and one that compresses a data stream
 * 
 * @author Securboration
 *
 */
@DfuAnnotation(
    functionalityBeingPerformed = ImageProcessor.class,
    resourceDependencies={
            Cpu.class,
            PhysicalMemoryResource.class
            }
    )
public class Example8_ImageProcessor {
    
    @FunctionalAspectAnnotation(
        aspect=AspectImageProcessorInitialize.class,
        aspectSpecificResourceDependencies={}
        )
    public void initialize(){}
    
    /**
     * Transform an image, (usually) resulting in a lower quality but smaller 
     * image.
     * 
     * @param image
     * @return
     */
    @FunctionalAspectAnnotation(
        aspect=AspectImageProcessorProcessImage.class
        )
    @ImageFidelityImpact(
        truthConstraint=TruthConstraint.USUALLY_TRUE,
        fidelityImpacts={
                ImpactType.DECREASES
                },
        fidelityDimensions={
                ImageFidelityType.FEATURE_SIZE_FIDELITY,
                ImageFidelityType.NOISE_FIDELITY,
                ImageFidelityType.PIXEL_FIDELITY
                }
        )
    @LossyTransformation
    public byte[] process(
            byte[] image
            ){return compress(image);}
    
    /**
     * Dummy compression method
     * 
     * @param image an image to perform lossy compression on
     * @return the compressed image
     */
    private static byte[] compress(byte[] image){return image;}
    
}
