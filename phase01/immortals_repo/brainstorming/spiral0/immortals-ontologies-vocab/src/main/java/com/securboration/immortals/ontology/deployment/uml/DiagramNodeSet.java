package com.securboration.immortals.ontology.deployment.uml;

/**
 * A node set abstraction
 * 
 * @author Securboration
 *
 */
public class DiagramNodeSet {
    
    /**
     * Describes the nodes in the set
     */
    private DiagramNode nodeSetDescriptor;
    
    /**
     * The nodes in the set
     */
    private DiagramNode[] nodes;

    public DiagramNode getNodeSetDescriptor() {
        return nodeSetDescriptor;
    }

    public void setNodeSetDescriptor(DiagramNode nodeSetDescriptor) {
        this.nodeSetDescriptor = nodeSetDescriptor;
    }

    public DiagramNode[] getNodes() {
        return nodes;
    }

    public void setNodes(DiagramNode[] nodes) {
        this.nodes = nodes;
    }
}
