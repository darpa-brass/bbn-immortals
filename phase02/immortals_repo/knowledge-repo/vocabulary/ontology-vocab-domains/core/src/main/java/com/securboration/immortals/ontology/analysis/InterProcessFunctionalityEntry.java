package com.securboration.immortals.ontology.analysis;

import com.securboration.immortals.ontology.core.HumanReadable;

/**
 * Where does this Functionality "begin"?
 */
public class InterProcessFunctionalityEntry implements HumanReadable {

    // Describe this entry point via method pointer
    private String methodPointer;
    // Describe this entry point in human readable terms
    private String humanReadable;

    public String getMethodPointer() {
        return methodPointer;
    }

    public void setMethodPointer(String methodPointer) {
        this.methodPointer = methodPointer;
    }

    @Override
    public String getHumanReadableDesc() {
        return humanReadable;
    }

    public void setHumanReadable(String humanReadable) {
        this.humanReadable = humanReadable;
    }
}
