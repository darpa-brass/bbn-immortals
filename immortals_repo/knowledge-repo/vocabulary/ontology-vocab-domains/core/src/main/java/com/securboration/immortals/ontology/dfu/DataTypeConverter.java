package com.securboration.immortals.ontology.dfu;

import com.securboration.immortals.ontology.functionality.datatype.DataType;

/**
 * A DFU whose purpose is the conversion between abstract datatypes
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "A DFU whose purpose is the conversion between abstract datatypes " +
    " @author jstaples ")
public class DataTypeConverter extends Dfu {

    /**
     * The type to convert from
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The type to convert from")
    private DataType fromDataType;
    
    /**
     * The type to convert to
     */
    @com.securboration.immortals.ontology.annotations.RdfsComment(
        "The type to convert to")
    private DataType toDataType;

    public DataType getFromDataType() {
        return fromDataType;
    }

    public void setFromDataType(DataType fromDataType) {
        this.fromDataType = fromDataType;
    }

    public DataType getToDataType() {
        return toDataType;
    }

    public void setToDataType(DataType toDataType) {
        this.toDataType = toDataType;
    }
    
}
