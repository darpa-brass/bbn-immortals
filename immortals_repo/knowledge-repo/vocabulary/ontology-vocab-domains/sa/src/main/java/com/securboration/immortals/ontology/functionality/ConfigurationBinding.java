package com.securboration.immortals.ontology.functionality;

import com.securboration.immortals.ontology.functionality.datatype.DataType;

public class ConfigurationBinding {
    
    private Class<? extends DataType> semanticType;
    
    private String binding;

    public Class<? extends DataType> getSemanticType() {
        return semanticType;
    }

    public void setSemanticType(Class<? extends DataType> semanticType) {
        this.semanticType = semanticType;
    }

    public String getBinding() {
        return binding;
    }

    public void setBinding(String binding) {
        this.binding = binding;
    }
}
