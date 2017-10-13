package com.securboration.immortals.bcad.dataflow;

import com.securboration.immortals.bcad.dataflow.StackEmulator.Entry;

public class DataLocationStack extends DataLocation {
    
    private final Entry stackEntry;

    public DataLocationStack(Entry stackEntry) {
        this.stackEntry = stackEntry;
    }

    
    public Entry getStackEntry() {
        return stackEntry;
    }


    @Override
    public String toString() {
        return "stack entry " + stackEntry.getId();
    }

}
