package com.securboration.immortals.ontology.functionality.dataproperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.securboration.immortals.ontology.constraint.BinaryComparisonOperatorType;
import com.securboration.immortals.ontology.functionality.datatype.DataProperty;

/**
 * A top-level abstraction of data fidelity
 * 
 * @author Securboration
 *
 */
public class Fidelity extends DataProperty {
    
    private QualitativeFidelityAssertion[] fidelityRelationships;

    
    public QualitativeFidelityAssertion[] getFidelityRelationships() {
        return fidelityRelationships;
    }

    
    public void setFidelityRelationships(
            QualitativeFidelityAssertion[] fidelityRelationships) {
        this.fidelityRelationships = fidelityRelationships;
    }
    
    
    //TODO: O(N) insert, terrible
    protected void addQualitativeFidelityAssertion(
            BinaryComparisonOperatorType operator, 
            Class<? extends Fidelity>... object
            ){

        if(fidelityRelationships == null){
            fidelityRelationships = new QualitativeFidelityAssertion[]{};
        }
        
        List<QualitativeFidelityAssertion> oldList = 
                Arrays.asList(fidelityRelationships);
        
        List<QualitativeFidelityAssertion> newList = 
                new ArrayList<>(oldList);
        
        QualitativeFidelityAssertion a = new QualitativeFidelityAssertion();
        a.setSubjectOfAssertion(this.getClass());
        a.setOperator(operator);
        a.setObjectOfAssertion(object);
        
        newList.add(a);
    }
    
}
