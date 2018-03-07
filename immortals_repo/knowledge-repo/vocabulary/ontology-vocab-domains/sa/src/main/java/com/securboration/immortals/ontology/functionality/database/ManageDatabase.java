package com.securboration.immortals.ontology.functionality.database;

import com.securboration.immortals.ontology.core.HumanReadable;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class ManageDatabase extends Functionality implements HumanReadable {
    @Override
    public String getHumanReadableDesc() {
        return "This describes a DFU that queries a database for information.";
    }
}
