package com.securboration.immortals.ontology.inference;

import java.util.ArrayList;
import java.util.List;

import com.securboration.immortals.ontology.core.HumanReadable;

/**
 * An inference rule consists of
 * <ol>
 * <li>zero or more data-agnostic <i>preconditions</i> (other inference rules
 * that must be executed prior to this one)</li>
 * <li>zero or more data-dependent <i>predicates</i> that must hold for the rule
 * to fire</li>
 * <li>a SPARQL query that possibly creates new triples when the above hold</li>
 * </ol>
 * 
 * @author jstaples
 *
 */
public class InferenceRule implements HumanReadable {
    
    private String humanReadableDesc;

    /**
     * Another inference rule that must be executed before this one (sequential
     * ordering). If multiple rules are specified, all must be executed before
     * this one.
     */
    private final List<InferenceRule> explicitPrecondition = new ArrayList<>();

    /**
     * A logical predicate that must be executed before this one (logical
     * ordering). If multiple predicates are specified, all must be executed
     * before this one.
     */
    private final List<AskQuery> predicate = new ArrayList<>();

    /**
     * A query whose execution possibly creates new triples
     */
    private ConstructQuery forwardInferenceRule;

    
    public ConstructQuery getForwardInferenceRule() {
        return forwardInferenceRule;
    }

    
    public void setForwardInferenceRule(ConstructQuery forwardInferenceRule) {
        this.forwardInferenceRule = forwardInferenceRule;
    }

    
    public List<InferenceRule> getExplicitPrecondition() {
        return explicitPrecondition;
    }

    
    public List<AskQuery> getPredicate() {
        return predicate;
    }


    @Override
    public String getHumanReadableDesc() {
        return humanReadableDesc;
    }
    
    public void setHumanReadableDesc(String humanReadableDesc){
        this.humanReadableDesc = humanReadableDesc;
    }
    
}

