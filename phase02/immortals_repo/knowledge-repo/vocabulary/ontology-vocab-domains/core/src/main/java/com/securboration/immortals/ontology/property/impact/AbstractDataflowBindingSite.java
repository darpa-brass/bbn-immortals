package com.securboration.immortals.ontology.property.impact;


import com.securboration.immortals.ontology.core.Resource;

public class AbstractDataflowBindingSite extends AssertionBindingSite {
    
    private Class<? extends Resource> src;
    
    private Class<? extends Resource> dest;

    public Class<? extends Resource> getSrc() {
        return src;
    }

    public void setSrc(Class<? extends Resource> src) {
        this.src = src;
    }

    public Class<? extends Resource> getDest() {
        return dest;
    }

    public void setDest(Class<? extends Resource> dest) {
        this.dest = dest;
    }
}
