package com.securboration.dfus.xml.transform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;

@DfuAnnotation(
		functionalityBeingPerformed=XmlTransformer.class,
		resourceDependencies = {}
)
public class XsltTransformer {
	
	private static TransformerFactory transformerFactory;
	
	private static synchronized TransformerFactory acquire() {
		if(transformerFactory != null) {
			return transformerFactory;
		}
		
		transformerFactory = TransformerFactory.newInstance();
		return transformerFactory;
	}

	@FunctionalAspectAnnotation(
			aspect=ApplyXsltAspect.class
	)
	public static String translate(
			final String xslt,
			final String xmlToTranslate
	) throws JAXBException, TransformerException {

		if (xslt.equals("no translation needed")) {
			return xmlToTranslate;
		}

		TransformerFactory tf = acquire();
		Transformer transformer = tf.newTransformer(new StreamSource(new ByteArrayInputStream(xslt.getBytes())));

		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.VERSION, "1.0");

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		transformer.transform(
				new StreamSource(new ByteArrayInputStream(xmlToTranslate.getBytes())),
				new StreamResult(out)
		);

		return out.toString();
	}

}
