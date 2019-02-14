package com.securboration.immortals.ontology.bytecode;

/**
 * A field in a class
 * 
 * @author Securboration
 *
 */
public class LocalVariable extends MethodStructure {
    
    /**
     * The name of the variable
     */
    private String name;
    
    /**
     * The declared type of the variable
     */
    private String declaredTypeDescriptor;
    
    /**
     * The variable's frame index
     */
    private int localIndex;
    
    /**
     * The line number on which the variable's scope begins
     */
    private Integer scopeBegin;
    
    /**
     * The line number on which the variable's scope ends
     */
    private Integer scopeEnd;

    
    public String getName() {
        return name;
    }

    
    public void setName(String name) {
        this.name = name;
    }

    
    public String getDeclaredTypeDescriptor() {
        return declaredTypeDescriptor;
    }

    
    public void setDeclaredTypeDescriptor(String declaredTypeDescriptor) {
        this.declaredTypeDescriptor = declaredTypeDescriptor;
    }

    
    public int getLocalIndex() {
        return localIndex;
    }

    
    public void setLocalIndex(int localIndex) {
        this.localIndex = localIndex;
    }

    
    public Integer getScopeBegin() {
        return scopeBegin;
    }

    
    public void setScopeBegin(Integer scopeBegin) {
        this.scopeBegin = scopeBegin;
    }

    
    public Integer getScopeEnd() {
        return scopeEnd;
    }

    
    public void setScopeEnd(Integer scopeEnd) {
        this.scopeEnd = scopeEnd;
    }
}
