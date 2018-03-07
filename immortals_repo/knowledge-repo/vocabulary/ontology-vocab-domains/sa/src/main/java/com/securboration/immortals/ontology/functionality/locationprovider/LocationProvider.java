package com.securboration.immortals.ontology.functionality.locationprovider;

import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

/**
 * Provides a location
 * 
 * @author Securboration
 *
 */
@ConceptInstance
public class LocationProvider extends Functionality {
    
    public LocationProvider() {
        this.setFunctionalityId("LocationProvider");
        this.setFunctionalAspects(new FunctionalAspect[]{
                new CleanupAspect(),
                new InitializeAspect(),
                new GetLastKnownLocationAspect(),
                new GetCurrentLocationAspect(),
                new HighAccuracyLocationProvider()
        });
    }
    
    
    
    

}
