package com.securboration.immortals.ontology.functionality.alg.encryption;

import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;
import com.securboration.immortals.ontology.functionality.aspects.ConfigAspectBase;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class AspectCipherConfig extends ConfigAspectBase {
    public AspectCipherConfig() {
        super("aspect-cipher-config");
        super.setInputs(new Input[]{
                Input.getInput(EncryptionKey.class),
                Input.getInput(InitializationVector.class),
        });
        super.setOutputs(new Output[]{
        });
    }
}
