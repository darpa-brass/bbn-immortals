package com.securboration.immortals.deployment.parser2;

public class RelationshipEdge {

    private final String nodeId;
    private final int minCardinality;
    private final int maxCardinatlity;
    
    public String getNodeId() {
        return nodeId;
    }
    public int getMinCardinality() {
        return minCardinality;
    }
    public int getMaxCardinatlity() {
        return maxCardinatlity;
    }
    public RelationshipEdge(
            String nodeId, 
            int minCardinality,
            int maxCardinatlity
            ) {
        super();
        this.nodeId = nodeId;
        this.minCardinality = minCardinality;
        this.maxCardinatlity = maxCardinatlity;
    }
    
}
