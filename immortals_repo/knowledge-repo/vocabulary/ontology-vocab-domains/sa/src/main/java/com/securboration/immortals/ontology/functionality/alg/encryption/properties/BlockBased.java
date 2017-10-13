package com.securboration.immortals.ontology.functionality.alg.encryption.properties;

import com.securboration.immortals.ontology.algorithm.AlgorithmConfigurationProperty;

/**
 * Indicates that the algorithm operates on a block of data (compared to, for 
 * example, a stream of data).
 * 
 * @author Securboration
 *
 */
public class BlockBased extends AlgorithmConfigurationProperty {
    
    private int blockSize;
    
    public BlockBased(){}

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }
    
}
