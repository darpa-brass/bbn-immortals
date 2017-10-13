package com.securboration.immortals.ontology.functionality.logger;

import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class AspectLoggerCleanup extends DefaultAspectBase {
    public AspectLoggerCleanup(){
        super("loggerCleanup");
    }
}