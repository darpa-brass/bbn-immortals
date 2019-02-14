package com.securboration.immortals.ontology.pattern.spec;

/**
 * 
 * 
 * @author jstaples
 *
 */
public class ParadigmComponent {
    
    /**
     * Uniquely and durably identifies an instance of a concept
     */
    private String durableId;
    
    /**
     * The component's ordering in a sequence of components
     */
    private int ordering;
    
    /**
     * The multiplicity of the component
     */
    private String multiplicityOperator;

    
    public String getDurableId() {
        return durableId;
    }

    
    public void setDurableId(String durableId) {
        this.durableId = durableId;
    }


    
    public int getOrdering() {
        return ordering;
    }


    
    public void setOrdering(int ordering) {
        this.ordering = ordering;
    }


    
    public String getMultiplicityOperator() {
        return multiplicityOperator;
    }


    
    public void setMultiplicityOperator(String multiplicityOperator) {
        this.multiplicityOperator = multiplicityOperator;
    }
    
    

}
