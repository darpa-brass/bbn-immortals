package com.securboration.immortals.ontology.resources.streams;

import com.securboration.immortals.ontology.functionality.aspects.DefaultAspectBase;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class WrapOutputStreamWithCipher extends DefaultAspectBase {
    public WrapOutputStreamWithCipher() {
        super("wrap-encrypted-stream-with-cipher");
    }
}
