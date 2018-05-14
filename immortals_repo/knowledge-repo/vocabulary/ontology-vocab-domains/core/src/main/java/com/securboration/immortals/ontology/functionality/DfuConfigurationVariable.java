package com.securboration.immortals.ontology.functionality;

import com.securboration.immortals.ontology.functionality.datatype.DataType;

public class DfuConfigurationVariable {
    
    private Class<? extends DataType> semanticType;
    
    private String magicStringVar;

    public Class<? extends DataType> getSemanticType() {
        return semanticType;
    }

    public void setSemanticType(Class<? extends DataType> semanticType) {
        this.semanticType = semanticType;
    }

    public String getMagicStringVar() {
        return magicStringVar;
    }

    public void setMagicStringVar(String magicStringVar) {
        this.magicStringVar = magicStringVar;
    }
}
