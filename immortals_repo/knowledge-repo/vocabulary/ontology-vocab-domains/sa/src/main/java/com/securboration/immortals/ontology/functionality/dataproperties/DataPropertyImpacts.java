package com.securboration.immortals.ontology.functionality.dataproperties;

import com.securboration.immortals.ontology.functionality.datatype.DataProperty;

/**
 * A binding of fidelity dimensions to impacts on those dimensions
 * 
 * @author Securboration
 *
 */
public class DataPropertyImpacts extends DataProperty {

    private DataPropertyImpact[] impacts;

    
    public DataPropertyImpact[] getImpacts() {
        return impacts;
    }

    
    public void setImpacts(DataPropertyImpact[] impacts) {
        this.impacts = impacts;
    }
    
}
