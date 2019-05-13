package com.securboration.immortals.ontology.functionality.aspects;

import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;

public class InitAspectBase extends DefaultAspectBase {
    public InitAspectBase(final String name){
        super(name);
    }
    
    public InitAspectBase() {
        super();
    }
    
    public InitAspectBase(
            final String name, 
            Input[] inputs,
            Output[] outputs
            ){
        super(name,inputs,outputs);
    }
}