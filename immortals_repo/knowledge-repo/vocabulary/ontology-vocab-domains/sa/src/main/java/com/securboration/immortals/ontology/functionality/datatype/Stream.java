package com.securboration.immortals.ontology.functionality.datatype;

import com.securboration.immortals.ontology.functionality.DesignPattern;

public class Stream extends DataType {
    
    private DesignPattern designPattern;

    public DesignPattern getDesignPattern() {
        return designPattern;
    }

    public void setDesignPattern(DesignPattern designPattern) {
        this.designPattern = designPattern;
    }
}
