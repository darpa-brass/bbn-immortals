package com.securboration.immortals.ontology.bytecode.analysis;

import com.securboration.immortals.ontology.bytecode.InvocationType;

/**
 * A bytecode instruction that performs a method invocation
 * 
 * @author jstaples
 *
 */
public class MethodCall extends Instruction {
    
    /**
     * The name of the method being called
     */
    private String calledMethodName;
    
    /**
     * The desc of the method being called
     */
    private String calledMethodDesc;

    /**
     * The owner of the method being called
     */
    private String owner;

    /**
     * The invocation type of the method being called
     */
    private InvocationType invocationType;

    /**
     * If static analysis can prove the owner of the method, true. If owner is ambiguous, false.
     */
    public boolean ownerAssurance;

    /**
     * The order in which a method is called, e.g. if "println" is the first method called, its order will be 1.
     */
    private int order;
    
    private int lineNumber;

    public boolean isOwnerAssurance() {
        return ownerAssurance;
    }

    public void setOwnerAssurance(boolean ownerAssurance) {
        this.ownerAssurance = ownerAssurance;
    }

    public InvocationType getInvocationType() {
        return invocationType;
    }
    
    public void setInvocationType(InvocationType invocationType) {
        this.invocationType = invocationType;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
    
    public String getCalledMethodName() {
        return calledMethodName;
    }

    
    public void setCalledMethodName(String calledMethodName) {
        this.calledMethodName = calledMethodName;
    }

    
    public String getCalledMethodDesc() {
        return calledMethodDesc;
    }

    
    public void setCalledMethodDesc(String calledMethodDesc) {
        this.calledMethodDesc = calledMethodDesc;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
