package com.securboration.immortals.ontology.functionality.alg.encryption;

import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;
import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class AspectCipherCleanup extends DefaultAspectBase {
    public AspectCipherCleanup(){
        super("cipherCleanup");
        super.setInputs(new Input[]{
                });
        super.setOutputs(new Output[]{
                });
    }
}