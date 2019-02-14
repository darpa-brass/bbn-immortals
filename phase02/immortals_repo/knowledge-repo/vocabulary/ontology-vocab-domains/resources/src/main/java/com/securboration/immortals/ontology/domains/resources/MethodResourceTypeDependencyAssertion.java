package com.securboration.immortals.ontology.domains.resources;

import com.securboration.immortals.ontology.core.Resource;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class MethodResourceTypeDependencyAssertion {
    
    private String methodName;
    private String methodDesc;
    private String methodOwner;
    private String dependencyAssurance;
    private MethodResourceTypeDependencyAssertion originAssertion;
    
    private Class<? extends Resource> resourceUtilized;

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

    public String getMethodOwner() {
        return methodOwner;
    }

    public void setMethodOwner(String methodOwner) {
        this.methodOwner = methodOwner;
    }

    public Class<? extends Resource> getResourceUtilized() {
        return resourceUtilized;
    }

    public void setResourceUtilized(Class<? extends Resource> resourceUtilized) {
        this.resourceUtilized = resourceUtilized;
    }

    public String getDependencyAssurance() {
        return dependencyAssurance;
    }

    public void setDependencyAssurance(String dependencyAssurance) {
        this.dependencyAssurance = dependencyAssurance;
    }

    public MethodResourceTypeDependencyAssertion getOriginAssertion() {
        return originAssertion;
    }

    public void setOriginAssertion(MethodResourceTypeDependencyAssertion originAssertion) {
        this.originAssertion = originAssertion;
    }
    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof MethodResourceTypeDependencyAssertion)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        MethodResourceTypeDependencyAssertion typeAssert = (MethodResourceTypeDependencyAssertion) obj;
        return new EqualsBuilder().append(methodName, typeAssert.methodName)
                .append(methodDesc, typeAssert.methodDesc)
                .append(methodOwner, typeAssert.methodOwner)
                .isEquals();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(methodName)
                .append(methodDesc).append(methodOwner).toHashCode();
    }
}
