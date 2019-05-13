package com.securboration.immortals.ontology.functionality.xml;

import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.resources.logical.LogicalResource;


@ConceptInstance
public class XsltEmbedStrategy extends LogicalResource {

    private RetrievalStrategy retrievalStrategy;

    public RetrievalStrategy getRetrievalStrategy() {
        return retrievalStrategy;
    }

    public void setRetrievalStrategy(RetrievalStrategy retrievalStrategy) {
        this.retrievalStrategy = retrievalStrategy;
    }
}
