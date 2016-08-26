package com.securboration.immortals.ontology.functionality;

import com.securboration.immortals.ontology.bytecode.AClass;
import com.securboration.immortals.ontology.functionality.datatype.DataType;

/**
 * A mechanism by which a DFU reads and writes data
 * 
 * @author Securboration
 *
 */
public class DataFlowMechanism {

    /**
     * The bytecode construct providing the dataflow mechanism
     * 
     * E.g., could be a value on the stack, a field, etc.
     */
    private AClass bytecodeConstruct;

    /**
     * The semantic type of the data being passed
     */
    private DataType type;

    public AClass getBytecodeConstruct() {
        return bytecodeConstruct;
    }

    public void setBytecodeConstruct(AClass bytecodeConstruct) {
        this.bytecodeConstruct = bytecodeConstruct;
    }

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }
}
