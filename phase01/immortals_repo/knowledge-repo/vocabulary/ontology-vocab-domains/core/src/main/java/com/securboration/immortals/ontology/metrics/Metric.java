package com.securboration.immortals.ontology.metrics;

import com.securboration.immortals.ontology.core.Resource;

public class Metric {
    
    private MeasurementType measurementType;
    private String value;
    private String unit;
    private String linkId;
    
    private Resource applicableResourceInstance;
    private Class<? extends Resource> applicableResourceType;
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }

    
    public MeasurementType getMeasurementType() {
        return measurementType;
    }

    
    public void setMeasurementType(MeasurementType type) {
        this.measurementType = type;
    }

    
    public Resource getApplicableResourceInstance() {
        return applicableResourceInstance;
    }

    
    public void setApplicableResourceInstance(Resource applicableResourceInstance) {
        this.applicableResourceInstance = applicableResourceInstance;
    }

    
    public Class<? extends Resource> getApplicableResourceType() {
        return applicableResourceType;
    }

    
    public void setApplicableResourceType(
            Class<? extends Resource> applicableResourceType) {
        this.applicableResourceType = applicableResourceType;
    }

    
    public String getLinkId() {
        return linkId;
    }

    
    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

}
