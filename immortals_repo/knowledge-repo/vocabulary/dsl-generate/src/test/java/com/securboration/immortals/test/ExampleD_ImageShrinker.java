package com.securboration.immortals.test;

import com.securboration.immortals.ontology.functionality.compression.LossyTransformation;
import com.securboration.immortals.ontology.functionality.dataproperties.ImpactType;
import com.securboration.immortals.ontology.functionality.dataproperties.MemoryFootprint;
import com.securboration.immortals.ontology.functionality.dataproperties.PixelFidelity;
import com.securboration.immortals.ontology.functionality.imageprocessor.AspectImageProcessorCleanup;
import com.securboration.immortals.ontology.functionality.imageprocessor.AspectImageProcessorInitialize;
import com.securboration.immortals.ontology.functionality.imageprocessor.AspectImageProcessorProcessImage;
import com.securboration.immortals.ontology.functionality.imageprocessor.ImageProcessor;

import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.dataproperties.ImpactOfInvocation;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.dataproperties.ImpactsOfInvocation;

/**
 * An image shrinking DFU
 * 
 * @author Securboration
 *
 */
@DfuAnnotation(
    functionalityBeingPerformed = ImageProcessor.class
    )
public class ExampleD_ImageShrinker {
    
    @FunctionalAspectAnnotation(aspect=AspectImageProcessorInitialize.class)
    public void initialize(){}
    
    @FunctionalAspectAnnotation(aspect=AspectImageProcessorCleanup.class)
    public void cleanup(){}
    
    /**
     * Shrink (via resampling) an image, resulting in the following changes:
     * <p>
     * <ol>
     *   <li>Pixel fidelity (# of visible pixels) of the returned image 
     *       DECREASES</li>
     *   <li>Memory footprint of the returned image DECREASES</li>
     *   <li>The returned image carries the LOSSY_TRANSFORM property</li>
     * </ol>
     * 
     * @param image
     * @return
     */
    @FunctionalAspectAnnotation(
        aspect=AspectImageProcessorProcessImage.class
        )
    public byte[] process(
            @ImpactsOfInvocation(
                impacts={
                    @ImpactOfInvocation(
                        impactOfInvocation=ImpactType.DECREASES,
                        impactedProperties={
                                PixelFidelity.class,
                                MemoryFootprint.class
                            }
                        ),
                    @ImpactOfInvocation(
                        impactOfInvocation=ImpactType.ADDS,
                        impactedProperties={
                                LossyTransformation.class
                            }
                        )
                    }
                )//these all bind to arg0: image
            byte[] image
            ){return compress(image);}
    
    private byte[] compress(byte[] image){return image;}
    
}


