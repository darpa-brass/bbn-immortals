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
public class Cipher extends Functionality {
    
    public Cipher() {
        this.setFunctionalityId("Cipher");
        this.setFunctionalAspects(
            new FunctionalAspect[]{
                new AspectCipherCleanup(),
                new AspectCipherDecrypt(),
                new AspectCipherEncrypt(),
                new AspectCipherInitialize(),
            });
    }
    
    
    
    

}
