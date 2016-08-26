package com.securboration.immortals.ontology.functionality;

import com.securboration.immortals.ontology.functionality.datatype.DataType;

/**
 * The output of a DFU
 * 
 * @author Securboration
 *
 */
public class Output extends DataFlow {
    
    public static Output getOutput(Class<? extends DataType> type){
        Output o = new Output();
        o.setType(type);
        return o;
    }
    
}
