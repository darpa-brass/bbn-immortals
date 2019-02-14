package com.securboration.immortals.bcad.dataflow;

import com.securboration.immortals.bcad.dataflow.DataflowHelper.LocalVariableSpec;

public class DataLocationLocal extends DataLocation {
    
    private final LocalVariableSpec local;

    @Override
    public String toString() {
        return "local " + local.getLocalIndex();
    }

    public DataLocationLocal(LocalVariableSpec local) {
        this.local = local;
    }

}
