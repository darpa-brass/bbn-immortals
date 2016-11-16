package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.annotations.triples.Literal;
import com.securboration.immortals.ontology.annotations.triples.Triple;
import com.securboration.immortals.ontology.constraint.InvocationCriterionType;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.uris.Uris.rdfs;

@Triple(
    predicateUri=rdfs.comment$,
    objectLiteral=@Literal(
        "Models an invocation as a change driver"
        )
    )
public class InvocationCriterion extends CriterionStatement {

    private InvocationCriterionType criterion;
    private Class<? extends FunctionalAspect> invokedAspect;
    
    public InvocationCriterionType getCriterion() {
        return criterion;
    }
    
    public void setCriterion(InvocationCriterionType criterion) {
        this.criterion = criterion;
    }

    
    public Class<? extends FunctionalAspect> getInvokedAspect() {
        return invokedAspect;
    }

    
    public void setInvokedAspect(Class<? extends FunctionalAspect> invokedAspect) {
        this.invokedAspect = invokedAspect;
    }
    
    
}
