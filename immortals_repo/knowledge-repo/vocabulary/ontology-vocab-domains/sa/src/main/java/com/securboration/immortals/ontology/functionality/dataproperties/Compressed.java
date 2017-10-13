package com.securboration.immortals.ontology.functionality.dataproperties;

import com.securboration.immortals.ontology.algorithm.Algorithm;
import com.securboration.immortals.ontology.functionality.datatype.DataProperty;

/**
 * A top-level abstraction of compression
 * 
 * @author Securboration
 *
 */
public class Compressed extends DataProperty {
    
    public Compressed() {
        super.setHidden(true);
    }

    private Class<? extends Algorithm> compressionAlgorithm;

    public Class<? extends Algorithm> getCompressionAlgorithm() {
        return compressionAlgorithm;
    }

    public void setCompressionAlgorithm(
            Class<? extends Algorithm> compressionAlgorithm) {
        this.compressionAlgorithm = compressionAlgorithm;
    }
}
