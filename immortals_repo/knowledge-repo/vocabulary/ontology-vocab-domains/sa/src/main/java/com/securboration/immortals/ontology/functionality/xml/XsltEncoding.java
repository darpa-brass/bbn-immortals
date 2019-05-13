package com.securboration.immortals.ontology.functionality.xml;

import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.resources.logical.LogicalResource;

@ConceptInstance
public class XsltEncoding extends LogicalResource {

    private CharacterEncoding characterEncoding;

    public CharacterEncoding getCharacterEncoding() {
        return characterEncoding;
    }

    public void setCharacterEncoding(CharacterEncoding characterEncoding) {
        this.characterEncoding = characterEncoding;
    }
}
