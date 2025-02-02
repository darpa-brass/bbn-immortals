package com.securboration.immortals.ontology.functionality.dataproperties;

import com.securboration.immortals.ontology.algorithm.Algorithm;
import com.securboration.immortals.ontology.functionality.ConfidentialProperty;
import com.securboration.immortals.ontology.functionality.datatype.DataProperty;

/**
 * A top-level abstraction of encryption
 * 
 * @author Securboration
 *
 */
public class Encrypted extends ConfidentialProperty {
    
    public Encrypted() {
        super.setHidden(true);
    }

    private Class<? extends Algorithm> encryptionAlgorithm;

    public Class<? extends Algorithm> getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(
            Class<? extends Algorithm> encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }
    
}
