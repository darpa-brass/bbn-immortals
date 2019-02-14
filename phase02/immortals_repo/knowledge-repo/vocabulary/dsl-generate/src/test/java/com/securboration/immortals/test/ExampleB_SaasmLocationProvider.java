package com.securboration.immortals.test;

import com.securboration.immortals.ontology.functionality.locationprovider.CleanupAspect;
import com.securboration.immortals.ontology.functionality.locationprovider.GetLastKnownLocationAspect;
import com.securboration.immortals.ontology.functionality.locationprovider.InitializeAspect;
import com.securboration.immortals.ontology.functionality.locationprovider.LocationProvider;
import com.securboration.immortals.ontology.resources.NetworkResource;
import com.securboration.immortals.ontology.resources.gps.GpsSatellite;
import com.securboration.immortals.ontology.resources.gps.SaasmReceiver;

import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;

/**
 * A DFU implementing various LocationProvider functional aspects.
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
public class ExampleB_SaasmLocationProvider {
    
    //we could infer that this is an @Location by dataflow analysis on the 
    // getLastKnownLocation method
    private volatile String lastKnownLocation;
    
    /**
     * Initialize
     */
    @FunctionalAspectAnnotation(aspect=InitializeAspect.class)
    public void buildup(){/* ... */}
    
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
