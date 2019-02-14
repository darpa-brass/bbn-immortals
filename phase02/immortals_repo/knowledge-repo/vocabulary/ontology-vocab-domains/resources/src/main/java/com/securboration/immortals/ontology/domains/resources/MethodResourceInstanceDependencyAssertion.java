package com.securboration.immortals.ontology.domains.resources;

import com.securboration.immortals.ontology.core.Resource;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class MethodResourceInstanceDependencyAssertion {
    
    private String consumerPointer;
    private MethodResourceInstanceDependencyAssertion originAssertion;
    private String dependencyAssurance;
    
    private Resource resourceConsumed;

    public String getConsumerPointer() {
        return consumerPointer;
    }

    public void setConsumerPointer(String consumerPointer) {
        this.consumerPointer = consumerPointer;
    }

    public Resource getResourceConsumed() {
        return resourceConsumed;
    }

    public void setResourceConsumed(Resource resourceConsumed) {
        this.resourceConsumed = resourceConsumed;
    }

    public MethodResourceInstanceDependencyAssertion getOriginAssertion() {
        return originAssertion;
    }

    public void setOriginAssertion(MethodResourceInstanceDependencyAssertion originAssertion) {
        this.originAssertion = originAssertion;
    }

    public String getDependencyAssurance() {
        return dependencyAssurance;
    }

    public void setDependencyAssurance(String dependencyAssurance) {
        this.dependencyAssurance = dependencyAssurance;
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof MethodResourceInstanceDependencyAssertion)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        MethodResourceInstanceDependencyAssertion instanceAssert = (MethodResourceInstanceDependencyAssertion) obj;
        return new EqualsBuilder().append(consumerPointer, instanceAssert.consumerPointer)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).append(consumerPointer).toHashCode();
    }
}
