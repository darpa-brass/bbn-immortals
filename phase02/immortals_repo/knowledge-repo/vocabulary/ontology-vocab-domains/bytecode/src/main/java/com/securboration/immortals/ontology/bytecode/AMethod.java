package com.securboration.immortals.ontology.bytecode;

import com.securboration.immortals.ontology.bytecode.analysis.BasicBlockDecomposition;
import com.securboration.immortals.ontology.bytecode.analysis.Instruction;

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
    
    /**
     * Describes instructions interesting for a high-level analysis of the
     * software. E.g., method calls are interesting whereas stack manipulation
     * like DUP_X2 isn't.
     */
    private Instruction[] interestingInstructions;
    
    /**
     * A basic block-oriented view of the instructions that make up this method
     */
    private BasicBlockDecomposition basicBlockAnalysis;
    
    /**
     * The arguments to the method
     */
    private MethodArg[] methodArgs;

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

    
    public Instruction[] getInterestingInstructions() {
        return interestingInstructions;
    }

    
    public void setInterestingInstructions(Instruction[] interestingInstructions) {
        this.interestingInstructions = interestingInstructions;
    }

    
    public BasicBlockDecomposition getBasicBlockAnalysis() {
        return basicBlockAnalysis;
    }

    
    public void setBasicBlockAnalysis(BasicBlockDecomposition basicBlockAnalysis) {
        this.basicBlockAnalysis = basicBlockAnalysis;
    }

    
    public MethodArg[] getMethodArgs() {
        return methodArgs;
    }

    
    public void setMethodArgs(MethodArg[] methodArgs) {
        this.methodArgs = methodArgs;
    }
}
