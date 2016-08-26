package com.securboration.immortals.ontology.functionality;

import com.securboration.immortals.ontology.functionality.datatype.DataType;

/**
 * The output of a DFU
 * 
 * @author Securboration
 *
 */
public class Output {
    
    /**
     * The a datatype to be provided as output
     */
    private Class<? extends DataType> type;

    public Class<? extends DataType> getType() {
        return type;
    }

    public void setType(Class<? extends DataType> type) {
        this.type = type;
    }
    
}
