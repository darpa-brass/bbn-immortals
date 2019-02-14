package com.securboration.immortals.ontology.property.impact;

import com.securboration.immortals.ontology.constraint.XmlResourceImpactType;
import com.securboration.immortals.ontology.core.Resource;

public class XmlResourceImpact extends ResourceImpact {

    private XmlResourceImpactType xmlResourceImpactType;

    private Class<? extends Resource> targetResource;

    public XmlResourceImpactType getXmlResourceImpactType() {
        return xmlResourceImpactType;
    }

    public void setXmlResourceImpactType(XmlResourceImpactType xmlResourceImpactType) {
        this.xmlResourceImpactType = xmlResourceImpactType;
    }

    public Class<? extends Resource> getTargetResource() {
        return targetResource;
    }

    public void setTargetResource(Class<? extends Resource> targetResource) {
        this.targetResource = targetResource;
    }
}
