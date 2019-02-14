package com.securboration.immortals.ontology.pattern.spec;

/**
 * Describes the abstract steps in a sequence of operations required to perform
 * a logical task as manifested in code. For example, using many software
 * components requires first a declaration of some object, second an
 * initialization of that object, the actual use of the object, and finally
 * cleanup.
 * 
 * @author jstaples
 *
 */
public class AbstractUsageParadigm {
    
    /**
     * Uniquely and durably identifies an instance of a concept
     */
    private String durableId;
    
    /**
     * The components of the paradigm.  Each corresponds to a specific code 
     * construct.  For example, declaration.
     */
    private ParadigmComponent[] component;

    
    public ParadigmComponent[] getComponent() {
        return component;
    }

    
    public void setComponent(ParadigmComponent[] component) {
        this.component = component;
    }


    
    public String getDurableId() {
        return durableId;
    }


    
    public void setDurableId(String durableId) {
        this.durableId = durableId;
    }
    
    

}
