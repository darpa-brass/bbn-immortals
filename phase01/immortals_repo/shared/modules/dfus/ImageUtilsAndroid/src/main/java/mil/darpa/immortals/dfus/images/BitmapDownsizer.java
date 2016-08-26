package mil.darpa.immortals.dfus.images;

import android.graphics.Bitmap;
import com.securboration.immortals.ontology.core.TruthConstraint;
import com.securboration.immortals.ontology.functionality.dataproperties.ImageFidelityType;
import com.securboration.immortals.ontology.functionality.dataproperties.ImpactType;
import com.securboration.immortals.ontology.functionality.imageprocessor.AspectImageProcessorProcessImage;
import com.securboration.immortals.ontology.functionality.imageprocessor.ImageProcessor;
import com.securboration.immortals.ontology.resources.compute.Cpu;
import com.securboration.immortals.ontology.resources.memory.PhysicalMemoryResource;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.compression.LossyTransformation;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.dataproperties.ImageFidelityImpact;
import mil.darpa.immortals.core.synthesis.ObjectPipe;
import mil.darpa.immortals.core.synthesis.interfaces.ReadableObjectPipeInterface;
import mil.darpa.immortals.core.synthesis.interfaces.WriteableObjectPipeInterface;

/**
 * Created by awellman@bbn.com on 6/22/16.
 */
@DfuAnnotation(
        functionalityBeingPerformed = ImageProcessor.class,
        resourceDependencies = {
                Cpu.class,
                PhysicalMemoryResource.class
        }
)
public class BitmapDownsizer extends ObjectPipe<Bitmap, Bitmap> {

    private double targetMegapixels;

    @FunctionalAspectAnnotation(
            aspect = AspectImageProcessorProcessImage.class
    )
    @LossyTransformation
    public BitmapDownsizer(double targetMegapixels, WriteableObjectPipeInterface<Bitmap> next) {
        super(next);
        this.targetMegapixels = targetMegapixels;
    }

    @FunctionalAspectAnnotation(
            aspect = AspectImageProcessorProcessImage.class
    )
    @LossyTransformation
    public BitmapDownsizer(double targetMegapixels, ReadableObjectPipeInterface<Bitmap> previous) {
        super(previous);
        this.targetMegapixels = targetMegapixels;
    }

    @FunctionalAspectAnnotation(aspect = AspectImageProcessorProcessImage.class)
    @ImageFidelityImpact(
            truthConstraint = TruthConstraint.USUALLY_TRUE,
            fidelityImpacts = {
                    ImpactType.DECREASES
            },
            fidelityDimensions = {
                    ImageFidelityType.PIXEL_FIDELITY
            }
    )

    @Override
    protected Bitmap process(
            Bitmap bitmap
    ) {
        return ImageUtilsAndroid.resizeBitmap(bitmap, targetMegapixels);
    }

    @Override
    protected Bitmap flushToOutput() {
        return null;
    }

    @Override
    protected void preNextClose() {

    }

    @Override
    public int getBufferSize() {
        throw new RuntimeException("Not supported!");
    }
}
