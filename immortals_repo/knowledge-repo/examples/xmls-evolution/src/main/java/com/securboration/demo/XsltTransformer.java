package com.securboration.demo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XsltTransformer {
	
	private final String xslt;
	
	public XsltTransformer(String xslt) {
		this.xslt = xslt;
	}
	
	public String translate(String xml) throws JAXBException, TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer(new StreamSource(new ByteArrayInputStream(this.xslt.getBytes())));
        
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0");
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        transformer.transform(
        		new StreamSource(new ByteArrayInputStream(xml.getBytes())), 
        		new StreamResult(out)
        		);
        
        return out.toString();
	}

}
