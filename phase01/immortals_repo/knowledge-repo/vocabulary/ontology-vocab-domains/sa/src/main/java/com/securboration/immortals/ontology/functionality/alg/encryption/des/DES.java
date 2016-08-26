package com.securboration.immortals.ontology.functionality.alg.encryption.des;

import com.securboration.immortals.ontology.algorithm.Algorithm;
import com.securboration.immortals.ontology.functionality.alg.encryption.EncryptionAlgorithmBuilder;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

/**
 * An algorithm for doing something
 * 
 * @author Securboration
 *
 */
@ConceptInstance
public class DES extends Algorithm {
    
    public DES(){
        super.setProperties(
                EncryptionAlgorithmBuilder.getSymmetricBlockEncryptionAlgorithmProperties(
                        "nist",
                        "fips-46-3",
                        "http://csrc.nist.gov/publications/fips/fips46-3/fips46-3.pdf",
                        64, 
                        64
                        ));
    }

}
