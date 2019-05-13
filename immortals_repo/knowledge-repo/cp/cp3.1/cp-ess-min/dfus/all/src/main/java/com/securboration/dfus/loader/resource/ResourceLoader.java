package com.securboration.dfus.loader.resource;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.cp.java.ClasspathResource;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class ResourceLoader extends Functionality {

    public ResourceLoader() {
        super();
        this.setFunctionalityId("Resource Loader");
        Class<? extends Resource>[] resourceClassArray = new Class[1];
        resourceClassArray[0] = ClasspathResource.class;
        this.setResourceDependencies(resourceClassArray);
    }

}



