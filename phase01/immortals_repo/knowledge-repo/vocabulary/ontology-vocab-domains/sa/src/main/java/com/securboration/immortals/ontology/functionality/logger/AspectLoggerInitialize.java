package com.securboration.immortals.ontology.functionality.logger;

import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class AspectLoggerInitialize extends DefaultAspectBase {
    public AspectLoggerInitialize(){
        super("loggerInit");
    }
}