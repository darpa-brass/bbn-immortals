package com.securboration.immortals.exampleDataflows;

import com.securboration.immortals.ontology.gmei.ApplicationArchitecture;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class CPP_ESS_ARCH extends ApplicationArchitecture {

    public CPP_ESS_ARCH() {
        this.setSubProjects(new ApplicationArchitecture[]{new CPP_ESS_MIN_CLIENT(), new CPP_ESS_MIN_SERVER()});
    }

}
