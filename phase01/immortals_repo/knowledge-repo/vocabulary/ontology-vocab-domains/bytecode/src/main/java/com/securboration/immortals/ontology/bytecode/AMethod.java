package com.securboration.immortals.ontology.bytecode;

/**
 * A method in a class
 * 
 * @author Securboration
 *
 */
public class AMethod extends ClassStructure {

    /**
     * A unique identifier for this object
     */
    private String bytecodePointer;

    /**
     * The name of the method. Together with the method signature, forms a
     * uniquely identifying tuple for the method in this class
     */
    private String methodName;

    /**
     * The type descriptor (binary signature) of the method. Together with the
     * method name, forms a uniquely identifying tuple for the method in this
     * class
     */
    private String methodDesc;
    
    /**
     * A human readable representation of the method's bytecode
     */
    private String bytecode;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public void setMethodDesc(String methodDesc) {
        this.methodDesc = methodDesc;
    }

    public String getBytecodePointer() {
        return bytecodePointer;
    }

    public void setBytecodePointer(String bytecodePointer) {
        this.bytecodePointer = bytecodePointer;
    }

    public String getBytecode() {
        return bytecode;
    }

    public void setBytecode(String bytecode) {
        this.bytecode = bytecode;
    }
}
