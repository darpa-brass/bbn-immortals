package com.securboration.immortals.deployment.pojos;

import java.util.Collection;

/**
 * Interface to be implemented by a deployment parser
 * 
 * @author jstaples
 *
 */
public interface DeploymentParser {

    public void parse(String deploymentJson);

    public Collection<TypeAbstraction> getTypes();

    public Collection<ObjectInstance> getInstances();

}
