package com.securboration.immortals.test;

import com.securboration.immortals.ontology.functionality.compression.JpegCompression;
import com.securboration.immortals.ontology.functionality.dataformat.Exif;
import com.securboration.immortals.ontology.functionality.datatype.Location;
import com.securboration.immortals.ontology.functionality.datatype.time.Time;

import mil.darpa.immortals.annotation.dsl.ontology.functionality.dataproperties.Compressed;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.dataproperties.HasMetadata;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.Image;

public class Example2_image {
    
    /**
     * Process a JPEG image containing metadata about the location and time 
     * the image was captured
     * 
     * @param image
     * @return
     */
    public void processImage(
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
            byte[] image
            ){}
    
    
}
