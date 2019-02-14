package com.securboration.immortals.ontology;

import com.securboration.immortals.ontology.annotations.RdfsComment;
import com.securboration.immortals.ontology.core.HumanReadable;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;

@GenerateAnnotation
public class FunctionalityTest implements HumanReadable {

    @RdfsComment("functionality being tested.")
    private Class <? extends Functionality> functionality;
    
    private String humanReadableDesc;
    
    public Class<? extends Functionality> getFunctionality() {
        return functionality;
    }
    public void setFunctionality(Class<? extends Functionality> functionality) {
        this.functionality = functionality;
    }

    @Override
    public String getHumanReadableDesc() {
        return humanReadableDesc;
    }

    public void setHumanReadableDesc(String humanReadableDesc) {
        this.humanReadableDesc = humanReadableDesc;
    }
}
