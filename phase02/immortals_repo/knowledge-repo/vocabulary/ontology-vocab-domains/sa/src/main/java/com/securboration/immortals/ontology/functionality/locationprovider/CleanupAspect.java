package com.securboration.immortals.ontology.functionality.locationprovider;

import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class CleanupAspect extends DefaultAspectBase {
    public CleanupAspect(){
        super("locationProviderInit");
    }
}