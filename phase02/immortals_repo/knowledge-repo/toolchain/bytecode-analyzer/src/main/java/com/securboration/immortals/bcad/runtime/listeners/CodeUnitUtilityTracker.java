package com.securboration.immortals.bcad.runtime.listeners;

import java.util.HashSet;
import java.util.Set;

import com.securboration.immortals.bcad.runtime.IEventListener;

public class CodeUnitUtilityTracker implements IEventListener {
    
    private final Set<String> usefulMethods = new HashSet<>();

    @Override
    public void reset() {
        usefulMethods.clear();
    }

    @Override
    public boolean postEntry(String methodHash) {
        usefulMethods.add(methodHash);
        return true;
    }

    @Override
    public boolean preReturn(String methodHash) {
        return true;
    }

    @Override
    public boolean postCatch(String methodHash, Throwable t) {
        return true;
    }

    @Override
    public boolean uncaught(String methodHash, Throwable t) {
        return true;
    }

    @Override
    public boolean postControlFlowPathTaken(String pathId) {
        return true;
    }

    
    public Set<String> getUsefulMethods() {
        return usefulMethods;
    }

}
