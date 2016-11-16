package com.securboration.immortals.test.cp2;

import com.securboration.immortals.ontology.functionality.imagecapture.AspectReadImage;
import com.securboration.immortals.ontology.functionality.imagecapture.ImageFileIO;
import com.securboration.immortals.ontology.resources.DiskResource;

import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;

/**
 * Adapted from
 * shared/modules/dfus/ImageUtilsAndroid/src/main/java/mil/darpa/immortals/dfus/
 * images/BitmapReader.java
 * 
 * @author jstaples
 *
 */
@DfuAnnotation(
    functionalityBeingPerformed = 
            ImageFileIO.class,
    resourceDependencies = {
            DiskResource.class
    }
)
public class BitmapReader implements ConsumingPipe<String> {
    
    private static class Bitmap{
        
    }
    
    private static class BitmapFactory{
        private static Bitmap decodeFile(String input){
            return null;
        }
    }
    
    private ConsumingPipe<Bitmap> next;

    public BitmapReader(ConsumingPipe<Bitmap> next) {
        this.next = next;
    }

    /**
     * From SA domain knowledge we know that this aspect
     *  accepts as input: a FileHandle
     *  produces as output: an Image
     */
    @FunctionalAspectAnnotation(
        aspect=AspectReadImage.class
        )
    @Override
    public void consume(String input) {
        Bitmap output = BitmapFactory.decodeFile(input);
        next.consume(output);
    }

    @Override
    public void flushPipe() {
        next.flushPipe();
    }

    @Override
    public void closePipe() {
        next.closePipe();
    }
}