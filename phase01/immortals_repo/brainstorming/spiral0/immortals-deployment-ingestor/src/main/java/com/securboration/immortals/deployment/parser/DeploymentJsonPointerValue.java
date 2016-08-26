package com.securboration.immortals.deployment.parser;

public class DeploymentJsonPointerValue{
    
    String pointerValue;
    Integer max;
    Integer min;
    
    public DeploymentJsonPointerValue(){}

    public String getPointerValue() {
        return pointerValue;
    }

    public void setPointerValue(String pointerValue) {
        this.pointerValue = pointerValue;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }
    
}