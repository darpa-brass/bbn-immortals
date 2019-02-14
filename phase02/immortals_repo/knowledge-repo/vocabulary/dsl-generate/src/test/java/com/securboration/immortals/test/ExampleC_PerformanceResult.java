package com.securboration.immortals.test;

import com.securboration.immortals.ontology.functionality.locationprovider.CleanupAspect;
import com.securboration.immortals.ontology.functionality.locationprovider.GetLastKnownLocationAspect;
import com.securboration.immortals.ontology.functionality.locationprovider.InitializeAspect;
import com.securboration.immortals.ontology.functionality.locationprovider.LocationProvider;
import com.securboration.immortals.ontology.profiling.MetricType;
import com.securboration.immortals.ontology.resources.NetworkResource;
import com.securboration.immortals.ontology.resources.gps.GpsSatellite;
import com.securboration.immortals.ontology.resources.gps.SaasmReceiver;

import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.profiling.MetricValue;
import mil.darpa.immortals.annotation.dsl.ontology.profiling.PerformanceResult;
import mil.darpa.immortals.annotation.dsl.ontology.profiling.PerformanceResults;
import mil.darpa.immortals.annotation.dsl.ontology.profiling.TestPlatformInfo;
import mil.darpa.immortals.annotation.dsl.ontology.profiling.Value;

/**
 * Illustrates @PerformanceResult
 * 
 * Note: this annotation is not currently parsed into a higher-level model 
 * (though annotations of this type will appear in the structural bytecode 
 * model of an annotated class).
 * 
 * @author Securboration
 *
 */

//This annotation can bind to a DFU or to a DFU's functional aspect.  The latter
// probably makes more sense
@PerformanceResults(results={
        @PerformanceResult(
            testPlatform=@TestPlatformInfo(platformInfo="Kali Linux 2016-1"),
            metricValues={
                    @MetricValue(
                        metric=MetricType.BYTES_READ,
                        value=@Value(stringValue="1234")
                        ),
                    @MetricValue(
                        metric=MetricType.BYTES_WRITTEN,
                        value=@Value(stringValue="12")
                        ),
                    @MetricValue(
                        metric=MetricType.CPU_CYCLES_SYSTEM,
                        value=@Value(stringValue="1234567")
                        ),
                    @MetricValue(
                        metric=MetricType.CPU_CYCLES_USER,
                        value=@Value(stringValue="1234567")
                        ),
                    @MetricValue(
                        metric=MetricType.CPU_CYCLES_WALL,
                        value=@Value(stringValue="1234567")
                        )
            })
        //could be repeated if multiple tests are performed
})
@DfuAnnotation(
        functionalityBeingPerformed = LocationProvider.class,
        resourceDependencies={
                GpsSatellite.class,
                SaasmReceiver.class,
                NetworkResource.class
                },
        tag="test"
        )
public class ExampleC_PerformanceResult {
    
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
