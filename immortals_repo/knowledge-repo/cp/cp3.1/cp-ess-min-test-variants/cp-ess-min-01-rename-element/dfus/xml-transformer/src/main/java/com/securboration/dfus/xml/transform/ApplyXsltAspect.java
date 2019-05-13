package com.securboration.dfus.xml.transform;

import com.securboration.immortals.ontology.constraint.XmlResourceImpactType;
import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Input;
import com.securboration.immortals.ontology.functionality.Output;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;
import com.securboration.immortals.ontology.property.impact.FormattedDataImpact;
import com.securboration.immortals.ontology.property.impact.FormattedDataImpactType;
import com.securboration.immortals.ontology.property.impact.ImpactStatement;
import com.securboration.immortals.ontology.property.impact.XmlResourceImpact;
import com.securboration.immortals.ontology.resources.Client;
import com.securboration.immortals.ontology.resources.Server;
import com.securboration.immortals.ontology.resources.xml.XmlInstance;

@ConceptInstance
public class ApplyXsltAspect extends FunctionalAspect {

	public ApplyXsltAspect() {
		super();
		this.setAspectId("applyXsltTransformation");
		this.setImpactStatements(new ImpactStatement[] {impact(), impact2()});
		this.setInputs(new Input[] {getXmlInput(),getXslt()});
		this.setOutputs(new Output[] {getXmlOutput()});
	}

	private static Input getXslt() {
		Input i = new Input();

		i.setFlowName("an XSLT document provided as input");
		i.setType(XmlInstance.class);

		return i;
	}

	private static Input getXmlInput() {
		Input i = new Input();

		i.setFlowName("an XML document provided as input");
		i.setType(XmlInstance.class);

		return i;
	}

	private static Output getXmlOutput() {
		Output i = new Output();

		i.setFlowName("an XML document emitted as output");
		i.setType(XmlInstance.class);

		return i;
	}

	private static ImpactStatement impact() {
		ImpactStatement is = new ImpactStatement();
		is.setHumanReadableDescription("an XSLT transform makes an arbitrary modification to an XML document");

		return is;
	}

	private static ImpactStatement impact2() {

		FormattedDataImpact formattedDataImpact = new FormattedDataImpact();
		formattedDataImpact.setImpactedData(XmlInstance.class);
		formattedDataImpact.setImpactType(FormattedDataImpactType.FORMAT_CHANGE);

		XmlResourceImpact resourceImpact = new XmlResourceImpact();
		resourceImpact.setImpactedResource(Client.class);
		resourceImpact.setTargetResource(Server.class);
		resourceImpact.setXmlResourceImpactType(XmlResourceImpactType.XML_INSTANCE_CHANGE);
		formattedDataImpact.setApplicableResource(resourceImpact);

		return formattedDataImpact;
	}

}
