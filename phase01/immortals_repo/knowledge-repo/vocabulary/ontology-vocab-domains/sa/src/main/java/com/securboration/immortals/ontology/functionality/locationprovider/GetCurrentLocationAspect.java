package com.securboration.immortals.ontology.functionality.locationprovider;

import com.securboration.immortals.ontology.functionality.Output;
import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.functionality.datatype.Location;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class GetCurrentLocationAspect extends DefaultAspectBase{
    public GetCurrentLocationAspect(){
        super("getCurrentLocation");
        super.setOutputs(new Output[]{location()});
    }
    
    private static Output location(){
        Output o = new Output();
        o.setType(Location.class);
        return o;
    }
}