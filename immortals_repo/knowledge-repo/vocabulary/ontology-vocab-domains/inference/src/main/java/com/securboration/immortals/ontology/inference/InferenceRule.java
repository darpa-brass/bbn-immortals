package com.securboration.immortals.ontology.inference;

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
public class InferenceRule {

    /**
     * Another inference rule that must be executed before this one (sequential
     * ordering). If multiple rules are specified, all must be executed before
     * this one.
     */
    private InferenceRule[] explicitPrecondition;

    /**
     * A logical predicate that must be executed before this one (logical
     * ordering). If multiple predicates are specified, all must be executed
     * before this one.
     */
    private AskQuery[] predicate;

    /**
     * A query whose execution possibly creates new triples
     */
    private ConstructQuery forwardInferenceRule;

    
    public InferenceRule[] getExplicitPrecondition() {
        return explicitPrecondition;
    }

    
    public void setExplicitPrecondition(InferenceRule[] explicitPrecondition) {
        this.explicitPrecondition = explicitPrecondition;
    }

    
    public AskQuery[] getPredicate() {
        return predicate;
    }

    
    public void setPredicate(AskQuery[] predicate) {
        this.predicate = predicate;
    }

    
    public ConstructQuery getForwardInferenceRule() {
        return forwardInferenceRule;
    }

    
    public void setForwardInferenceRule(ConstructQuery forwardInferenceRule) {
        this.forwardInferenceRule = forwardInferenceRule;
    }
    
}

