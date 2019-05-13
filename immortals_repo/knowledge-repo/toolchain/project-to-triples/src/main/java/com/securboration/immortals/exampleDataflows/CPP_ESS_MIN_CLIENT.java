package com.securboration.immortals.exampleDataflows;

import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.xml.AspectConstructXml;
import com.securboration.immortals.ontology.functionality.xml.RetrievalStrategy;
import com.securboration.immortals.ontology.functionality.xml.XsltEmbedStrategy;
import com.securboration.immortals.ontology.gmei.ApplicationArchitecture;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.resources.Client;

@ConceptInstance
public class CPP_ESS_MIN_CLIENT extends ApplicationArchitecture {

    public CPP_ESS_MIN_CLIENT() {
        BytecodeArtifactCoordinate clientCoord = new BytecodeArtifactCoordinate();
        clientCoord.setArtifactId("client");
        clientCoord.setGroupId("cp-ess-min");
        clientCoord.setVersion("unspecified");

        Client client = new Client();
        XsltEmbedStrategy xsltEmbedStrategy = new XsltEmbedStrategy();
        xsltEmbedStrategy.setRetrievalStrategy(RetrievalStrategy.FROM_CLASSPATH_RESOURCE);

        //TODO need to declare Client, can't realistically derive that info from application
        this.setAvailableResources(new Resource[]{client, xsltEmbedStrategy});
        this.setProjectCoordinate(clientCoord);
        this.setFunctionalAspects(new FunctionalAspect[]{new AspectConstructXml()});
    }

}
