package com.securboration.immortals.o2t;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class UniqueMethodCall {

    public String methodName;
    public String methodDesc;
    public String className;

    public UniqueMethodCall(String _methodName, String _methodDesc, String _className) {
        methodName = _methodName;
        methodDesc = _methodDesc;
        className = _className;
    }

    @Override
    public boolean equals(Object obj) {
        
        if (!(obj instanceof UniqueMethodCall)) {
            return false;
        }
        
        if (obj == this) {
            return true;
        }
        
        UniqueMethodCall mCall = (UniqueMethodCall) obj;
        return new EqualsBuilder().append(methodName, mCall.methodName)
                .append(methodDesc, mCall.methodDesc)
                .append(className, mCall.className)
                .isEquals();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(methodName)
                .append(methodDesc).append(className).toHashCode();
    }
}
