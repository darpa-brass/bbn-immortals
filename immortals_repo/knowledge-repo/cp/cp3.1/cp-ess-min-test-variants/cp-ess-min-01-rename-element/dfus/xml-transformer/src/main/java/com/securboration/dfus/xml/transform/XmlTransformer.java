package com.securboration.dfus.xml.transform;

import com.securboration.immortals.ontology.functionality.FunctionalAspect;
import com.securboration.immortals.ontology.functionality.Functionality;
import com.securboration.immortals.ontology.pojos.markup.ConceptInstance;

@ConceptInstance
public class XmlTransformer extends Functionality{
	
	public XmlTransformer() {
		super();
		
		this.setFunctionalityId("XmlTransformer");
		this.setFunctionalAspects(new FunctionalAspect[] {new ApplyXsltAspect()});
	}

}
