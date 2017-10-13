package com.securboration.immortals.pojoapi.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.securboration.immortals.ontology.functionality.imagescaling.ImageScalingFactor;
import com.securboration.immortals.ontology.measurement.CodeUnitPointer;
import com.securboration.immortals.ontology.measurement.MetricProfile;
import com.securboration.immortals.ontology.measurement.MetricSet;
import com.securboration.immortals.ontology.measurement.cp1cp2.InputImageSizeMegapixels;
import com.securboration.immortals.ontology.measurement.cp1cp2.OutputImageSizeMegapixels;
import com.securboration.immortals.ontology.property.Property;
import com.securboration.immortals.pojoapi.Triplifier;

public class TriplifierExampleMeasurements {
    
    public static void main(String[] args) throws IOException{
        
        Triplifier.serializeToTriples(
            generateMetrics(), 
            new File("./target/mockdata/testMeasurements.ttl").getAbsolutePath()
            );
        
    }
    
    private static class AtAk{

        @SuppressWarnings("unused")
        public synchronized Object scaryMethod(
                String arg1, 
                byte[][][] arg2,
                short[][][][][][][][][][][][][][][] arg3,
                List<Map<Long,Map<String,byte[]>[]>> arg4
                ) {return new Object();};
    }
    
    private static MetricSet generateMetrics() throws IOException {
        
        final CodeUnitPointer imageReaderDfu = 
                ExamplePointerHelper.getPointer(
                    AtAk.class, 
                    "scaryMethod"
                    );
        
        MetricSet metricSet = new MetricSet();
        metricSet.setProfiles(new MetricProfile[] {
                buildProfile(imageReaderDfu, 5d, 3.75d),
                buildProfile(imageReaderDfu, 8d, 6d),
                buildProfile(imageReaderDfu, 12d, 9d),
                buildProfile(imageReaderDfu, 16d, 12d),
                buildProfile(imageReaderDfu, 24d, 18d),
                buildProfile(imageReaderDfu, 5d, 2.5d),
                buildProfile(imageReaderDfu, 8d, 4d),
                buildProfile(imageReaderDfu, 12d, 6d),
                buildProfile(imageReaderDfu, 16d, 8d),
                buildProfile(imageReaderDfu, 24d, 12d),
                buildProfile(imageReaderDfu, 5d, 1.25d),
                buildProfile(imageReaderDfu, 8d, 2d),
                buildProfile(imageReaderDfu, 12d, 3d),
                buildProfile(imageReaderDfu, 16d, 4d),
                buildProfile(imageReaderDfu, 24d, 6d)
        });

        return metricSet;
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
