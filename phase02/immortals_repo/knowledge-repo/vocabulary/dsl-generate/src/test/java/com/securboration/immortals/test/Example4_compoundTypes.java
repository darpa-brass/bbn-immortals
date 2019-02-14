package com.securboration.immortals.test;

import com.securboration.immortals.ontology.functionality.compression.JpegCompression;
import com.securboration.immortals.ontology.functionality.dataformat.Exif;
import com.securboration.immortals.ontology.functionality.datatype.Location;
import com.securboration.immortals.ontology.functionality.datatype.time.Time;

import mil.darpa.immortals.annotation.dsl.ontology.functionality.dataproperties.Compressed;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.dataproperties.HasMetadata;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.Image;

public class Example4_compoundTypes {
    
    /**
     * Process a JPEG image containing metadata about the location and time 
     * the image was captured.  The image is extracted from a complex data 
     * structure.
     * 
     * @param image
     * @return
     */
    public void processImage(
            @Selector(ognlExpression="wrappedValues[0]")
            @Image
            @Compressed(
                    compressionAlgorithm=JpegCompression.class
                    )
            @HasMetadata(
                    metadataFormat=Exif.class,
                    metadataContent={
                            Location.class,
                            Time.class
                            })
            WrapperClass dataContainer
            ){}
    
    
    /**
     * Example of a non-trivial business logic class containing multiple data
     * types (an image and an audio clip).
     * 
     * @author Securboration
     *
     */
    private static class WrapperClass{
        
        private final Object[] wrappedValues;
        
        WrapperClass(byte[] image, byte[] audioClip){
            wrappedValues = new Object[]{
                    image,
                    audioClip
            };
        }
    }
    
    private static @interface Selector{
        /**
         * See <a
         * href=https://commons.apache.org/proper/commons-ognl/language-guide.
         * html>ognl</a>
         * 
         * @return
         */
        String ognlExpression();
    }
    
}

