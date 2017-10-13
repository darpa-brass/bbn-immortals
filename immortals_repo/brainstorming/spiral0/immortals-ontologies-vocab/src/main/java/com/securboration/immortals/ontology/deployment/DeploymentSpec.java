package com.securboration.immortals.ontology.deployment;

/**
 * A deployment spec
 * @author Securboration
 *
 */
public class DeploymentSpec extends Artifact{

    /**
     * The concurrency mode of the deployment
     */
    private ConcurrencyMode concurrencyMode;
    
    /**
     * The transaction mode of the deployment
     */
    private TransactionMode transactionMode;
    
}
