package com.securboration.immortals.ontology.functionality.logger;

import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

/**
 * Processes an image in some arbitrary manner
 * 
 * @author Securboration
 *
 */
@ConceptInstance
public class Logger extends Functionality {
    
    public Logger() {
        this.setFunctionalityId("Logger");
        this.setFunctionalAspects(new FunctionalAspect[]{
                new AspectLoggerCleanup(),
                new AspectLoggerInitialize(),
                new AspectLog(),
        });
    }
    
    
    
    

}
