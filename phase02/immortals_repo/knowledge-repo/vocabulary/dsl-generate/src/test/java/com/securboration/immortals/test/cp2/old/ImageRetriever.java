package com.securboration.immortals.test.cp2.old;

import java.io.File;

import com.securboration.immortals.ontology.functionality.imagecapture.AspectReadImage;
import com.securboration.immortals.ontology.functionality.imagecapture.AspectWriteImage;
import com.securboration.immortals.ontology.functionality.imagecapture.ImageFileIO;

import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;

/**
 * An image IO DFU
 * 
 * Two aspects are defined in the model:
 *   AspectReadImage
 *      inputs: a FileHandle
 *      outputs: an Image
 * 
 *   AspectWriteImage
 *      inputs: a FileHandle, an Image
 *      outputs: none
 * 
 * @author Securboration
 *
 */
@DfuAnnotation(
    functionalityBeingPerformed = ImageFileIO.class
    )
public class ImageRetriever {
    
    
    @FunctionalAspectAnnotation(
        aspect=AspectReadImage.class
        )
    //return type is implicitly dt:Image
    public byte[] getImage(
            //arg type is implicitly dt:FileHandle
            File imageToRead
            ){return null;}
    
    @FunctionalAspectAnnotation(
        aspect=AspectWriteImage.class
        )
    //return type is void
    public void writeImage(
            //arg type is implicitly dt:FileHandle
            File whereToWrite,
            //arg type is implicitly Image
            byte[] imageToWrite
            ){}
    
}


