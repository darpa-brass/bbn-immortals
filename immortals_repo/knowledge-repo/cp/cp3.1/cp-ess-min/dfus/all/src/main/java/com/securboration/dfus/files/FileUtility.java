package com.securboration.dfus.files;

import com.securboration.dfus.files.read.RetrieveFileResourceAspect;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.resources.FileSystemResource;

@ConceptInstance
public class FileUtility extends Functionality {
    public FileUtility() {
        super();
        this.setFunctionalityId("FileTransformer");
        this.setFunctionalAspects(new FunctionalAspect[]{new RetrieveFileResourceAspect()});
        Class<? extends Resource>[] resourceClassArray = new Class[1];
        resourceClassArray[0] = FileSystemResource.class;
        this.setResourceDependencies(resourceClassArray);
    }
}
