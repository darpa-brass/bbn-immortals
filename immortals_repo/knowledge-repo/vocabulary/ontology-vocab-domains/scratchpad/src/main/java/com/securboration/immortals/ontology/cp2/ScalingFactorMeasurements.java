package com.securboration.immortals.ontology.cp2;

import java.util.ArrayList;
import java.util.List;

import com.securboration.immortals.ontology.functionality.imageprocessor.ImageProcessor;
import com.securboration.immortals.ontology.functionality.imagescaling.ImageScalingFactor;
import com.securboration.immortals.ontology.functionality.imagescaling.ShrinkImage;
import com.securboration.immortals.ontology.measurement.CodeUnitPointer;
import com.securboration.immortals.ontology.measurement.DfuPointer;
import com.securboration.immortals.ontology.measurement.MetricProfile;
import com.securboration.immortals.ontology.measurement.MetricSet;
import com.securboration.immortals.ontology.measurement.cp1cp2.InputImageSizeMegapixels;
import com.securboration.immortals.ontology.measurement.cp1cp2.OutputImageSizeMegapixels;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.pojos.markup.Ignore;
import com.securboration.immortals.ontology.property.Property;

@Ignore
public class ScalingFactorMeasurements {
    
    @ConceptInstance
    public static class MetricSet1 extends MetricSet{
        public MetricSet1(){
            this.setProfiles(new MetricProfile[]{
                    buildProfile(Pointers.imageReaderDfu,5d, 3.75d),
                    buildProfile(Pointers.imageReaderDfu,8d, 6d),
                    buildProfile(Pointers.imageReaderDfu,12d, 9d),
                    buildProfile(Pointers.imageReaderDfu,16d, 12d),
                    buildProfile(Pointers.imageReaderDfu,24d, 18d),
                    buildProfile(Pointers.imageReaderDfu,5d, 2.5d),
                    buildProfile(Pointers.imageReaderDfu,8d, 4d),
                    buildProfile(Pointers.imageReaderDfu,12d, 6d),
                    buildProfile(Pointers.imageReaderDfu,16d, 8d),
                    buildProfile(Pointers.imageReaderDfu,24d, 12d),
                    buildProfile(Pointers.imageReaderDfu,5d, 1.25d),
                    buildProfile(Pointers.imageReaderDfu,8d, 2d),
                    buildProfile(Pointers.imageReaderDfu,12d, 3d),
                    buildProfile(Pointers.imageReaderDfu,16d, 4d),
                    buildProfile(Pointers.imageReaderDfu,24d, 6d)
            });
        }
        
        private static MetricProfile buildProfile(
                CodeUnitPointer pointer,
                double inSize,
                double outSize
                ){
            MetricProfile p = new MetricProfile();
            
            p.setMeasuredProperty(
                getScalingFactorMeasurements(
                    new double[]{
                            inSize,outSize
                            }
                    )
                );
            
            p.setCodeUnit(pointer);
            
            p.setHumanReadableDesc(
                String.format(
                    "An observation that for input image size %1.2fMP, an " +
                    "output image of size %1.2fMP was produced " +
                    "(scaling factor = %1.2f)", 
                    inSize,
                    outSize,
                    outSize/inSize
                    )
                );
            
            return p;
        }
    }
    
    @Ignore
    private static class Pointers{
        private static final DfuPointer imageReaderDfu = new DfuPointer();static{
            imageReaderDfu.setClassName(
                "com.bbn.immortals.ImageScalingClass"
                );
            imageReaderDfu.setMethodName("processImage(byte[] image)");
            imageReaderDfu.setRelevantFunctionalAspect(ShrinkImage.class);
            imageReaderDfu.setRelevantFunctionality(ImageProcessor.class);
        }
    }
    
    private static Property[] getScalingFactorMeasurements(
            double[] inputsToOutputs
            ){
        List<Property> measurements = new ArrayList<>();
        
        for(int i=0;i<inputsToOutputs.length;i+=2){
            final double inputSize = inputsToOutputs[i];
            final double outputSize = inputsToOutputs[i+1];
            final double scalingFactor = outputSize / inputSize;
            
            measurements.add(getInputSizeMeasurement(inputSize));
            measurements.add(getOutputSizeMeasurement(outputSize));
            measurements.add(getScalingFactorMeasurement(scalingFactor));
        }
        
        return measurements.toArray(new Property[0]);
    }
    
    private static Property getInputSizeMeasurement(
            final double size
            ){
        InputImageSizeMegapixels input = new InputImageSizeMegapixels();
        input.setNumMegapixels(size);
        
        return input;
    }
    
    private static Property getOutputSizeMeasurement(
            final double size
            ){
        OutputImageSizeMegapixels output = new OutputImageSizeMegapixels();
        output.setNumMegapixels(size);
        
        return output;
    }
    
    private static Property getScalingFactorMeasurement(
            final double scalingFactor
            ){
        ImageScalingFactor s = new ImageScalingFactor();
        s.setScalingFactor(scalingFactor);
        return s;
    }
    
}
