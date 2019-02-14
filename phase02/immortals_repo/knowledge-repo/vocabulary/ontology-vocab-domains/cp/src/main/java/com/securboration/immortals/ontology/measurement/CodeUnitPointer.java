package com.securboration.immortals.ontology.measurement;

/**
 * Mechanism by which a code unit can be indirectly referenced
 * 
 * @author jstaples
 *
 */
public class CodeUnitPointer {
    
    /**
     * An unambiguous pointer to a code unit.  E.g.,
     * 
     * <ul>
     * <li><b>A pointer to a class:</b> XOCOWVnMhrwamO03gbxdv4bGBquN7e4jP3UQLQDC1tc=</li>
     * <li><b>A pointer to a method:</b> XOCOWVnMhrwamO03gbxdv4bGBquN7e4jP3UQLQDC1tc=/methods/read()I</li>
     * <li><b>A pointer to a field:</b> XOCOWVnMhrwamO03gbxdv4bGBquN7e4jP3UQLQDC1tc=/fields/objectPipe</li>
     * </ul>
     */
    private String pointerString;
    
    /**
     * The internal name of a class.  
     * E.g., 
     * mil/darpa/immortals/core/synthesis/adapters/PipeToInputStream.class.
     * 
     * Note that there can be more than one class with the same name.
     */
    private String className;
    
    /**
     * The name of a method with no signature information.  
     * E.g., getAcquisitionTime.
     * 
     * Note that there can be more than one method with the same name.
     */
    private String methodName;
    
    public String getClassName() {
        return className;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    
    public String getPointerString() {
        return pointerString;
    }

    
    public void setPointerString(String pointerString) {
        this.pointerString = pointerString;
    }
    

}
