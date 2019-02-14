package com.securboration.immortals.test.cp2;

import com.securboration.immortals.ontology.functionality.imagescaling.ImageResizer;
import com.securboration.immortals.ontology.functionality.imagescaling.ShrinkImage;

import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;

/**
 * Adapted from
 * shared/modules/dfus/ImageUtilsAndroid/src/main/java/mil/darpa/immortals/dfus/
 * images/BitmapScaler.java
 * 
 * @author jstaples
 *
 */
@DfuAnnotation(
    functionalityBeingPerformed = ImageResizer.class
)
public class BitmapScaler implements ConsumingPipe<Bitmap> {
    
    private final ConsumingPipe<Bitmap> next;
    private final double scalingValue;

    public BitmapScaler(double scalingValue, ConsumingPipe<Bitmap> next) {
        this.next = next;
        this.scalingValue = scalingValue;
    }

    
    /**
     * We know from SA domain knowledge that this aspect
     *  accepts as input an Image
     *  accepts as input a ScalingFactor
     *  produces as output an Image
     */
    @FunctionalAspectAnnotation(
        aspect=ShrinkImage.class
        )
    @Override
    public void consume(Bitmap input) {
        Bitmap output = null;
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
