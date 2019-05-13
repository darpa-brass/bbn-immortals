package com.securboration.dfus.xmls.translate;

import com.securboration.immortals.ontology.constraint.XmlResourceImpactType;
import com.securboration.immortals.ontology.functionality.*;
import com.securboration.immortals.ontology.functionality.xml.CharacterEncoding;
import com.securboration.immortals.ontology.functionality.xml.XsltEmbedStrategy;
import com.securboration.immortals.ontology.functionality.xml.XsltEncoding;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.property.impact.*;
import com.securboration.immortals.ontology.resources.Client;
import com.securboration.immortals.ontology.resources.Server;
import com.securboration.immortals.ontology.resources.xml.XmlDocument;
import com.securboration.immortals.ontology.resources.xml.XsltFile;

@ConceptInstance
public class ApplyXsltAspect extends FunctionalAspect {

	public ApplyXsltAspect() {
		super();
		this.setAspectId("applyXsltTransformation");
		this.setImpactStatements(new ImpactStatement[] {impact2()});
		this.setInputs(new Input[] {getXmlInput(),getXslt()});
		this.setOutputs(new Output[] {getXmlOutput()});
		AspectConfiguration xsltEmbedStrat = new AspectConfiguration();
        xsltEmbedStrat.setRequiredResource(XsltEmbedStrategy.class);
        xsltEmbedStrat.setOptional(false);
		AspectConfiguration xsltEncoding = new AspectConfiguration();
		xsltEncoding.setRequiredResource(XsltEncoding.class);
		XsltEncoding defaultEncoding = new XsltEncoding();
		defaultEncoding.setCharacterEncoding(CharacterEncoding.UTF_8);

		xsltEncoding.setOptional(true);
		this.setAspectConfigurations(new AspectConfiguration[]{xsltEmbedStrat, xsltEncoding});
	}

	private static Input getXslt() {
		Input i = new Input();

		i.setFlowName("an XSLT document provided as input");
		i.setType(XsltFile.class);

		return i;
	}

	private static Input getXmlInput() {
		Input i = new Input();

		i.setFlowName("an XML document provided as input");
		i.setType(XmlDocument.class);

		return i;
	}

	private static Output getXmlOutput() {
		Output i = new Output();

		i.setFlowName("an XML document emitted as output");
		i.setType(XmlDocument.class);

		return i;
	}

	private static ImpactStatement impact2() {

		StructuredDocumentImpact structuredDocumentImpact = new StructuredDocumentImpact();
		structuredDocumentImpact.setImpactedData(XmlDocument.class);
		structuredDocumentImpact.setImpactType(StructuredDocumentImpactType.FORMAT_CHANGE);

		XmlResourceImpact resourceImpact = new XmlResourceImpact();
		resourceImpact.setImpactedResource(Client.class);
		resourceImpact.setTargetResource(Server.class);
		resourceImpact.setXmlResourceImpactType(XmlResourceImpactType.XML_INSTANCE_CHANGE);
		structuredDocumentImpact.setApplicableResource(resourceImpact);

		return structuredDocumentImpact;
	}

}
