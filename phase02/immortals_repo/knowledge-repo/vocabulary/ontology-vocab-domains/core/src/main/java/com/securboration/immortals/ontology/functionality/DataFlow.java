package com.securboration.immortals.ontology.functionality;

import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.property.Property;

/**
 * A model of data flow to/from a DFU
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A model of data flow to/from a DFU  @author jstaples ")
public class DataFlow {
    
    /**
     * The datatype that flows
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The datatype that flows")
    private Class<? extends DataType> type;
    
    /**
     * Properties of the flow
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "Properties of the flow")
    private Property[] properties;
    
    /**
     * The name of the flow
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The name of the flow")
    private String flowName;
    
    /**
     * Used to identify a flow within a spec. Links the semantic model of a
     * DataFlow to an arbitrary model defined in the spec String.
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
            "Used to identify a flow within a spec. Links the semantic model " +
            "of a DataFlow to an arbitrary model defined in the spec String.")
    private String specTag;

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

    
    public String getSpecTag() {
        return specTag;
    }

    
    public void setSpecTag(String specTag) {
        this.specTag = specTag;
    }
    
}
