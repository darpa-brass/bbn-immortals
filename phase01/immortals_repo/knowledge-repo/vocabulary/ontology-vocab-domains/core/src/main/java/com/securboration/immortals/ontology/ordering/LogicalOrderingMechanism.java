package com.securboration.immortals.ontology.ordering;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Describes the logical ordering of elements specified explicitly in " +
        "before/after form"
        )
    )
public class LogicalOrderingMechanism extends OrderingMechanism {
    
    private Class<?>[] comesBefore;
    private Class<?>[] comesAfter;
    
    public Class<?>[] getComesBefore() {
        return comesBefore;
    }
    
    public void setComesBefore(Class<?>[] comesBefore) {
        this.comesBefore = comesBefore;
    }
    
    public Class<?>[] getComesAfter() {
        return comesAfter;
    }
    
    public void setComesAfter(Class<?>[] comesAfter) {
        this.comesAfter = comesAfter;
    }

}
