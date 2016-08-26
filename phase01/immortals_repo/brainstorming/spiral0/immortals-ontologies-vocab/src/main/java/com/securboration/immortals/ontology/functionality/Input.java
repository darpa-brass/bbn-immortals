package com.securboration.immortals.ontology.functionality;

import com.securboration.immortals.ontology.functionality.datatype.DataType;

/**
 * Something that provides input to a DFU
 * @author Securboration
 *
 */
public class Input {
    
    /**
     * The a datatype to be provided as input
     */
    private Class<? extends DataType> type;

    public Class<? extends DataType> getType() {
        return type;
    }

    public void setType(Class<? extends DataType> type) {
        this.type = type;
    }
    
}
