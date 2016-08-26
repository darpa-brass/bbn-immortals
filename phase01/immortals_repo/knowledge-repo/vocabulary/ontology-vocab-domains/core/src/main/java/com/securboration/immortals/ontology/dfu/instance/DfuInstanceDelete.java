package com.securboration.immortals.ontology.dfu.instance;

import com.securboration.immortals.ontology.dfu.annotation.DfuAnnotation;

/**
 * An instantiation of a DFU that binds what abstract things are being done by
 * the DFU to how they are implemented in bytecode
 * 
 * @author Securboration
 *
 */
public class DfuInstanceDelete extends DfuAnnotation {
    
    /**
     * The location in code where the functionality is performed
     */
    private String codeUnitPointer;

    
    public String getCodeUnitPointer() {
        return codeUnitPointer;
    }
    
    public void setCodeUnitPointer(String codeUnitPointer) {
        this.codeUnitPointer = codeUnitPointer;
    }

}
