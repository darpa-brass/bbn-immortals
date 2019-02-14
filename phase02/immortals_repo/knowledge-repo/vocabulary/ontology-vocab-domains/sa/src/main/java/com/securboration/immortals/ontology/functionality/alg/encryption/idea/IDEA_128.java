package com.securboration.immortals.ontology.functionality.alg.encryption.idea;

import com.securboration.immortals.ontology.algorithm.Algorithm;
import com.securboration.immortals.ontology.functionality.alg.encryption.EncryptionAlgorithmBuilder;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class IDEA_128 extends Algorithm {
    public IDEA_128() {
        super.setProperties(
                EncryptionAlgorithmBuilder.getSymmetricBlockEncryptionAlgorithmProperties(
                        "hasler",
                        null,
                        null,
                        64,
                        128
                ));
    }
}
