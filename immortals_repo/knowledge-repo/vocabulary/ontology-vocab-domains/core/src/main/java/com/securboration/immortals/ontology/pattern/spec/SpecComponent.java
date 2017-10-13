package com.securboration.immortals.ontology.pattern.spec;

/**
 * Binds an abstract step in a logical sequence of events to an actual code
 * construct for achieving that step
 * 
 * @author jstaples
 *
 */
public class SpecComponent {
    
    /**
     * Uniquely and durably identifies an instance of a concept
     */
    private String durableId;

    /**
     * The code-level specification for a specific step in an aspect's use. This
     * is modeled semantically as a simple String, but in reality may be a
     * serialization of a much more complex format. Wherever interleaving of the
     * spec with semantic constructs is required (e.g., to specify that an
     * output of one function in a spec is used as input to another function in
     * another spec), the specId field of the DataFlow type should be used.
     */
    private String spec;

    /**
     * The logical usage paradigm implemented by the corresponding code
     * construct
     */
    private ParadigmComponent abstractComponentLinkage;

    
    public String getDurableId() {
        return durableId;
    }

    
    public void setDurableId(String durableId) {
        this.durableId = durableId;
    }

    
    public String getSpec() {
        return spec;
    }

    
    public void setSpec(String spec) {
        this.spec = spec;
    }

    
    public ParadigmComponent getAbstractComponentLinkage() {
        return abstractComponentLinkage;
    }

    
    public void setAbstractComponentLinkage(
            ParadigmComponent abstractComponentLinkage) {
        this.abstractComponentLinkage = abstractComponentLinkage;
    }
    

}
