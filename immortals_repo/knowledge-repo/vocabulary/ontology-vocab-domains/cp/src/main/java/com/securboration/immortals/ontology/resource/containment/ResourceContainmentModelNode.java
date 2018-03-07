package com.securboration.immortals.ontology.resource.containment;

import com.securboration.immortals.ontology.core.HumanReadable;

public class ResourceContainmentModelNode implements HumanReadable {
    
    private String humanReadableDesc;
    
    private ResourceContainmentModelNode[] containedNode;
    

    @Override
    public String getHumanReadableDesc() {
        return humanReadableDesc;
    }

    
    public void setHumanReadableDesc(String humanReadableDesc) {
        this.humanReadableDesc = humanReadableDesc;
    }


    
    public ResourceContainmentModelNode[] getContainedNode() {
        return containedNode;
    }


    
    public void setContainedNode(ResourceContainmentModelNode[] containedNode) {
        this.containedNode = containedNode;
    }

}
