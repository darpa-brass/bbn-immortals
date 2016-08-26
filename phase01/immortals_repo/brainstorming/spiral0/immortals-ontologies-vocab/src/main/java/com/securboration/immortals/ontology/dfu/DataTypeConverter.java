package com.securboration.immortals.ontology.dfu;

import com.securboration.immortals.ontology.functionality.datatype.DataType;

/**
 * A simple DFU that converts between types
 * 
 * @author Securboration
 *
 */
public class DataTypeConverter extends Dfu {

    /**
     * The type to convert from
     */
    private DataType fromDataType;
    
    /**
     * The type to convert to
     */
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
