package com.securboration.immortals.exampleDataflows;

import com.securboration.immortals.ontology.bytecode.BytecodeArtifactCoordinate;
import com.securboration.immortals.ontology.core.Resource;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.datatype.DataType;
import com.securboration.immortals.ontology.functionality.xml.AspectConstructXml;
import com.securboration.immortals.ontology.gmei.ApplicationArchitecture;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.property.impact.CauseEffectAssertion;
import com.securboration.immortals.ontology.resources.PlatformResource;
import com.securboration.immortals.ontology.resources.Server;
import com.securboration.immortals.ontology.resources.Software;
import com.securboration.immortals.ontology.resources.xml.XmlDocument;

@ConceptInstance
public class CPP_ESS_MIN_SERVER extends ApplicationArchitecture {

    public CPP_ESS_MIN_SERVER() {
        //TODO eventually need to infer
        XmlDocument xmlDocument = new XmlDocument();
        xmlDocument.setXmlVersion("1.0");
        xmlDocument.setEncoding("UTF-8");
        xmlDocument.setSchemaNamespace("http://inetprogram.org/projects/MDL");
        xmlDocument.setSchemaVersion("19");

        Server cppEssServer = new Server();
        Software serverSoftware = new Software();
        serverSoftware.setApplicationName("XML-Processor");
        serverSoftware.setDataInSoftware(new DataType[]{xmlDocument});
        cppEssServer.setResources(new PlatformResource[]{serverSoftware});

        BytecodeArtifactCoordinate serverCoord = new BytecodeArtifactCoordinate();
        serverCoord.setArtifactId("server");
        serverCoord.setGroupId("cp-ess-min");
        serverCoord.setVersion("unspecified");

        this.setAvailableResources(new Resource[]{cppEssServer});
        this.setProjectCoordinate(serverCoord);
        this.setCauseEffectAssertions(new CauseEffectAssertion[]{new ServerOrientedFormattedDataConstraint()});
        this.setFunctionalAspects(new FunctionalAspect[]{new AspectConstructXml()});
    }

}
