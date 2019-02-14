package com.securboration.immortals.ontology.functionality.alg.encryption;

import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

/**
 * A cipher can be used for encrypting or decrypting data
 * 
 * @author Securboration
 *
 */
@ConceptInstance
public class HashFunction extends Functionality {
    
    public HashFunction() {
        this.setFunctionalityId("HashFunction");
        this.setFunctionalAspects(
            new FunctionalAspect[]{
                new AspectCipherCleanup(),
                new AspectCipherDecrypt(),
                new AspectCipherEncrypt(),
                new AspectCipherInitialize(),
            });
    }
    
    
    
    

}
