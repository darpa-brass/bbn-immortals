package com.securboration.immortals.ontology.dfu;

import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.functionality.datatype.EncodedDataType;

/**
 * A DFU that encodes a datatype
 * @author Securboration
 *
 */
public class Encoder extends Dfu {

    /**
     * An input type, which may or may not be encoded
     */
    private DataType inputType;
    
    /**
     * An output type, which is encoded
     */
    private EncodedDataType outputType;

    public DataType getInputType() {
        return inputType;
    }

    public void setInputType(DataType inputType) {
        this.inputType = inputType;
    }

    public EncodedDataType getOutputType() {
        return outputType;
    }

    public void setOutputType(EncodedDataType outputType) {
        this.outputType = outputType;
    }
    
}
