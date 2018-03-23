package com.securboration.immortals.ontology.functionality.alg.encryption;

import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;
import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class AspectCipherInitialize extends DefaultAspectBase {
    public AspectCipherInitialize(){
        super("cipher-init");
        super.setInputs(new Input[]{
                Input.getInput(EncryptionKey.class)
                });
        super.setOutputs(new Output[]{
                });
    }
}