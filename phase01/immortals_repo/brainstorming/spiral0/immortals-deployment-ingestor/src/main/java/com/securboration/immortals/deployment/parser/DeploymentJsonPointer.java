package com.securboration.immortals.deployment.parser;

public class DeploymentJsonPointer{
    String pointerName;
    DeploymentJsonPointerValue[] pointerValues;
    
    private Integer max;
    private Integer min;
    
    public DeploymentJsonPointer(){}

    public String getPointerName() {
        return pointerName;
    }

    public void setPointerName(String pointerName) {
        this.pointerName = pointerName;
    }

    public DeploymentJsonPointerValue[] getPointerValues() {
        return pointerValues;
    }

    public void setPointerValues(DeploymentJsonPointerValue[] pointerValues) {
        this.pointerValues = pointerValues;
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