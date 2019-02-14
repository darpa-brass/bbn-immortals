package com.securboration.immortals.ontology.functionality.alg.encryption;

import com.securboration.immortals.ontology.algorithm.Algorithm;
import com.securboration.immortals.ontology.algorithm.AlgorithmStandardProperty;
import com.securboration.immortals.ontology.functionality.alg.encryption.properties.BlockBased;
import com.securboration.immortals.ontology.functionality.alg.encryption.properties.KeyLength;
import com.securboration.immortals.ontology.pojos.markup.GenerateAnnotation;

/**
 * A symmetric block cipher for encrypting a static chunk of data
 * 
 * @author Securboration
 *
 */
@GenerateAnnotation
public class SymmetricBlockCipher extends Algorithm {

    private AlgorithmStandardProperty encryptionSpec;
    private BlockBased blockSpec;
    private KeyLength keySpec;
    
    public SymmetricBlockCipher(){}

    public AlgorithmStandardProperty getEncryptionSpec() {
        return encryptionSpec;
    }

    public void setEncryptionSpec(AlgorithmStandardProperty encryptionSpec) {
        this.encryptionSpec = encryptionSpec;
    }

    public BlockBased getBlockSpec() {
        return blockSpec;
    }

    public void setBlockSpec(BlockBased blockSpec) {
        this.blockSpec = blockSpec;
    }

    public KeyLength getKeySpec() {
        return keySpec;
    }

    public void setKeySpec(KeyLength keySpec) {
        this.keySpec = keySpec;
    }
    
}
