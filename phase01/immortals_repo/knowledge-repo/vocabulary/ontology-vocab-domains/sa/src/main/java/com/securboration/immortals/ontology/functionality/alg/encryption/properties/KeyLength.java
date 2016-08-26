package com.securboration.immortals.ontology.functionality.alg.encryption.properties;

import com.securboration.immortals.ontology.algorithm.AlgorithmConfigurationProperty;

/**
 * A configuration property for key length
 * 
 * @author Securboration
 *
 */
public class KeyLength extends AlgorithmConfigurationProperty {

    private int keyLength;
    
    public KeyLength(){}

    public int getKeyLength() {
        return keyLength;
    }

    public void setKeyLength(int keyLength) {
        this.keyLength = keyLength;
    }
    
}
