package com.securboration.immortals.ontology.functionality.alg.encryption;

import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;
import com.securboration.immortals.ontology.functionality.aspects.InitAspectBase;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class AspectCipherInitialize extends InitAspectBase {
    public AspectCipherInitialize(){
        super("cipher-init");
        super.setInputs(new Input[]{
                Input.getInput(CipherAlgorithm.class),
                Input.getInput(CipherChainingMode.class),
                Input.getInput(CipherKeyLength.class),
                Input.getInput(PaddingScheme.class)
                });
        super.setOutputs(new Output[]{
                });
    }
}