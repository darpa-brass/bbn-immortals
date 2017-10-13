package com.securboration.immortals.ontology.functionality.dataproperties;

import com.securboration.immortals.ontology.functionality.dataproperties.ImpactType;
import com.securboration.immortals.ontology.functionality.datatype.DataProperty;

/**
 * A binding of fidelity dimensions to impacts on those dimensions
 * 
 * @author Securboration
 *
 */
public class DataPropertyImpact extends DataProperty {

    private Class<? extends DataProperty> propertyType;
    
    private ImpactType impact;

    
    public Class<? extends DataProperty> getPropertyType() {
        return propertyType;
    }

    
    public void setPropertyType(Class<? extends DataProperty> propertyType) {
        this.propertyType = propertyType;
    }

    
    public ImpactType getImpact() {
        return impact;
    }

    
    public void setImpact(ImpactType impact) {
        this.impact = impact;
    }
    
}
