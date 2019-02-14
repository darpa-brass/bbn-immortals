package com.securboration.immortals.ontology.bytecode;

/**
 * A method argument
 * 
 * @author Securboration
 *
 */
public class MethodArg extends LocalVariable {
    
    /**
     * The argument's number.  For instance methods, the implicit this keyword
     * is always at index 0, arg1 is at index 1, etc.  For static methods, the
     * first argument is at index 1, the second at index 2, etc. and there will
     * be no argument at index 0
     */
    private int argNumber;

    
    public int getArgNumber() {
        return argNumber;
    }

    
    public void setArgNumber(int argNumber) {
        this.argNumber = argNumber;
    }
    
    
}
