package com.securboration.immortals.ontology.functionality.aspects;

import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;

public class DefaultAspectBase extends FunctionalAspect{
    public DefaultAspectBase(final String name){
        super.setAspectId(name);
        super.setAspectSpecificResourceDependencies(new Class[]{});
        super.setInputs(new Input[]{});
        super.setOutputs(new Output[]{});
    }
    
    public DefaultAspectBase() {
        super();
    }
    
    public DefaultAspectBase(
            final String name, 
            Class[] aspectSpecificResourceDependencies, 
            Input[] inputs, 
            Output[] outputs
            ){
        super.setAspectId(name);
        super.setAspectSpecificResourceDependencies(aspectSpecificResourceDependencies);
        super.setInputs(inputs);
        super.setOutputs(outputs);
    }
}