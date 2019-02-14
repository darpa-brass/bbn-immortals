package com.securboration.immortals.test;

import com.securboration.immortals.ontology.functionality.locationprovider.CleanupAspect;
import com.securboration.immortals.ontology.functionality.locationprovider.GetLastKnownLocationAspect;
import com.securboration.immortals.ontology.functionality.locationprovider.InitializeAspect;
import com.securboration.immortals.ontology.functionality.locationprovider.LocationProvider;
import com.securboration.immortals.ontology.resources.NetworkResource;
import com.securboration.immortals.ontology.resources.gps.GpsSatellite;
import com.securboration.immortals.ontology.resources.gps.L1_C;
import com.securboration.immortals.ontology.resources.gps.SaasmReceiver;

import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.alg.encryption.EncryptionKey;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.alg.encryption.properties.Asymmetric;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.alg.encryption.properties.BlockBased;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.alg.encryption.properties.Symmetric;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.dataproperties.Compressed;
import mil.darpa.immortals.annotation.dsl.ontology.functionality.datatype.Image;

/**
 * Nonsensical annotations designed to test the parser.  Disregard this example
 * for any serious purpose.
 * 
 * @author Securboration
 *
 */
@DfuAnnotation(
        functionalityBeingPerformed = LocationProvider.class,
        resourceDependencies={
                GpsSatellite.class,
                SaasmReceiver.class,
                NetworkResource.class
                }
        )
@Symmetric
public class Example0_ParserStressTest {
    
    //we could infer that this is an @Location by dataflow analysis on the 
    // getLastKnownLocation method
    private volatile String lastKnownLocation;
    
    /**
     * Initialize
     */
    @FunctionalAspectAnnotation(
        aspect=InitializeAspect.class,
        aspectSpecificResourceDependencies={L1_C.class})
    @Asymmetric
    @BlockBased(blockSize=128)
    public void setup(
            @Image @Symmetric byte[] anImage,
            @EncryptionKey @Compressed byte[] key,
            int x,
            String y,
            String[] z
            ){/* ... */}
    
    /**
     * Cleanup
     */
    @FunctionalAspectAnnotation(aspect=CleanupAspect.class)
    public void teardown(){/* ... */}
    
    /**
     * GetLastKnownLocation
     * 
     * @return last known location
     */
    //note: Because the getLastKnownLocationAspect returns datatype.Location,
    //        the parser should be able to infer that the return type String has
    //        semantic type datatype.Location
    @FunctionalAspectAnnotation(aspect=GetLastKnownLocationAspect.class)
    public String getLastKnownLocation(){
        return lastKnownLocation;
    }
    
    /**
     * Dummy method that sets the last known location to some string value
     */
    public void somethingThatResultsInLocationBeingSet(){
        //...
        lastKnownLocation = "42.3601 N, 71.0589 W";
        //...
    }
    
}
