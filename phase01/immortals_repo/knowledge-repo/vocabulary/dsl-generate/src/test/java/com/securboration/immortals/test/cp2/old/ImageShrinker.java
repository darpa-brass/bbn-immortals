package com.securboration.immortals.test.cp2.old;

import com.securboration.immortals.ontology.functionality.imagescaling.EnlargeImage;
import com.securboration.immortals.ontology.functionality.imagescaling.ImageResizer;
import com.securboration.immortals.ontology.functionality.imagescaling.ShrinkImage;

import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;

/**
 * A lossy image resizing DFU
 * 
 * Two aspects are defined in the model:
 *   EnlargeImage
 *      inputs: an Image
 *      outputs: an Image
 *      impactOfInvocation: 
 *          output image has more pixels
 *          output image has data loss
 * 
 *   ShrinkImage
 *      inputs: an Image
 *      outputs: an Image
 *      impactOfInvocation: 
 *          output image has fewer pixels
 *          output image has data loss
 * 
 * @author Securboration
 *
 */
@DfuAnnotation(
    functionalityBeingPerformed = ImageResizer.class
    )
public class ImageShrinker {
    
    @FunctionalAspectAnnotation(
        aspect=ShrinkImage.class
        )
    //return type is implicitly dt:Image
    public byte[] shrink(
            //arg type is implicitly dt:Image
            byte[] inputImage
            ){return null;}
    
    @FunctionalAspectAnnotation(
        aspect=EnlargeImage.class
        )
    //return type is implicitly dt:Image
    public byte[] enlarge(
            //arg type is implicitly dt:Image
            byte[] inputImage
            ){return null;}
    
}


