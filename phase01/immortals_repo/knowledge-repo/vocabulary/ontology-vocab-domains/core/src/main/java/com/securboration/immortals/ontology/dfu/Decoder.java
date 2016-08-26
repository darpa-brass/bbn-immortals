package com.securboration.immortals.ontology.dfu;

import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.functionality.datatype.EncodedDataType;

/**
 * A DFU that decodes a datatype
 * @author Securboration
 *
 */
public class Decoder extends Dfu {

    /**
     * The input type, which is encoded
     */
    private EncodedDataType inputType;
    
    /**
     * The output type, which may or may not be encoded
     */
    private DataType outputType;

    public EncodedDataType getInputType() {
        return inputType;
    }

    public void setInputType(EncodedDataType inputType) {
        this.inputType = inputType;
    }

    public DataType getOutputType() {
        return outputType;
    }

    public void setOutputType(DataType outputType) {
        this.outputType = outputType;
    }
    
}
