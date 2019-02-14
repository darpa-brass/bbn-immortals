package com.securboration.immortals.ontology.functionality.alg.encryption.aes;

import com.securboration.immortals.ontology.algorithm.Algorithm;
import com.securboration.immortals.ontology.functionality.alg.encryption.EncryptionAlgorithmBuilder;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

/**
 * The Advanced Encryption Standard (Rijndael)
 * 
 * @author Securboration
 *
 */
@ConceptInstance
public class AES_256 extends Algorithm {
    
    public AES_256(){
        super.setProperties(
                EncryptionAlgorithmBuilder.getSymmetricBlockEncryptionAlgorithmProperties(
                        "nist",
                        "fips-197",
                        "http://csrc.nist.gov/publications/fips/fips197/fips-197.pdf",
                        128, 
                        256
                        ));
    }
    
    

}
