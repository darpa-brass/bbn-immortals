package com.securboration.client.utility;

import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class FileTransformer extends Functionality {
    public FileTransformer() {
        super();
        this.setFunctionalityId("FileTransformer");
        this.setFunctionalAspects(new FunctionalAspect[]{new RetrieveFileResourceAspect()});
    }
}
