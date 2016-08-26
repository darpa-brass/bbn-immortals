package com.securboration.immortals.ontology.functionality;

import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.property.Property;

/**
 * A model of data flow to/from a DFU
 * @author Securboration
 *
 */
public class DataFlow {
    
    /**
     * The datatype that flows
     */
    private Class<? extends DataType> type;
    
    /**
     * Properties of the flow
     */
    private Property[] properties;
    
    /**
     * The name of the flow
     */
    private String flowName;

    public Class<? extends DataType> getType() {
        return type;
    }

    public void setType(Class<? extends DataType> type) {
        this.type = type;
    }

    
    public Property[] getProperties() {
        return properties;
    }

    
    public void setProperties(Property[] properties) {
        this.properties = properties;
    }

    
    public String getFlowName() {
        return flowName;
    }

    
    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }
    
}
