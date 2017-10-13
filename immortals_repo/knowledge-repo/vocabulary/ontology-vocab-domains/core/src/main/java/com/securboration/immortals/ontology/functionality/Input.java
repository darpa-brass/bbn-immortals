package com.securboration.immortals.ontology.functionality;

import com.securboration.immortals.ontology.functionality.datatype.DataType;

/**
 * Something that provides input to a DFU
 * 
 * @author jstaples
 *
 */
@com.securboration.immortals.ontology.annotations.RdfsComment(
    "Something that provides input to a DFU  @author jstaples ")
public class Input extends DataFlow {
    
    public static Input getInput(Class<? extends DataType> type){
        Input i = new Input();
        i.setType(type);
        return i;
    }
}
