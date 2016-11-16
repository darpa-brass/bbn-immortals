package com.securboration.immortals.ontology.analysis;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "A node in a dataflow graph.  This could be a method call or a " +
        "DFU functional aspect, depending upon the granularity of the analysis"
        )
    )
public class DataflowNode {
    
    private Resource resourceTemplate;
    private Resource contextTemplate;


    
    public Resource getContextTemplate() {
        return contextTemplate;
    }


    
    public void setContextTemplate(Resource contextTemplate) {
        this.contextTemplate = contextTemplate;
    }



    
    public Resource getResourceTemplate() {
        return resourceTemplate;
    }



    
    public void setResourceTemplate(Resource resourceTemplate) {
        this.resourceTemplate = resourceTemplate;
    }
    
    
    
}
