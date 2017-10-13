package com.securboration.immortals.bcad.dataflow;

import com.securboration.immortals.bcad.dataflow.value.JavaValue;

/**
 * the movement of data from a src to a dest
 * 
 * @author jstaples
 *
 */
public class ActionMove extends Action {
    
    private final JavaValue data;
    
    private final DataLocation dataSource;
    
    private final DataLocation dataDest;
    
    
    @Override
    public String toString() {
        return String.format(
            "MOVE %s from [%s] to [%s]", 
            data,
            dataSource,
            dataDest
            );
    }


    public ActionMove(
            JavaValue data,
            DataLocation dataSource,
            DataLocation dataDest
            ) {
        super();
        this.dataSource = dataSource;
        this.dataDest = dataDest;
        this.data = data;
    }

}
