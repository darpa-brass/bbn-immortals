package com.securboration.immortals.exampleDataflows;

import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.xml.AspectConstructXml;
import com.securboration.immortals.ontology.gmei.ApplicationArchitecture;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.property.impact.CauseEffectAssertion;
import com.securboration.immortals.ontology.resources.Client;
import com.securboration.immortals.ontology.resources.PlatformResource;
import com.securboration.immortals.ontology.resources.Server;
import com.securboration.immortals.ontology.resources.logical.XMLSchema;

@ConceptInstance
public class CPP_ESS_ARCH extends ApplicationArchitecture {

    public CPP_ESS_ARCH() {

        Server cppEssServer = new Server();
        XMLSchema xmlSchemaServer = new XMLSchema();
        xmlSchemaServer.setVersion("2.0");
        cppEssServer.setResources(new PlatformResource[]{xmlSchemaServer});

        Client cppEssClient = new Client();
        XMLSchema xmlSchemaClient = new XMLSchema();
        xmlSchemaClient.setVersion("1.0");
        cppEssClient.setResources(new PlatformResource[]{xmlSchemaClient});

        this.setAvailableResources(new Resource[]{cppEssServer, cppEssClient});
        this.setCauseEffectAssertions(new CauseEffectAssertion[]{new ServerOrientedFormattedDataConstraint()});
        this.setFunctionalAspects(new FunctionalAspect[]{new AspectConstructXml()});
    }

}
