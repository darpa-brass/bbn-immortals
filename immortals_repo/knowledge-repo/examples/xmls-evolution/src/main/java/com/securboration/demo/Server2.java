package com.securboration.demo;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import com.securboration.generated.v2.ItemType;
import com.securboration.generated.v2.Todolist;

public class Server2 extends Server{

	@Override
	protected void doSomethingInternal(String xml) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(Todolist.class);
		Unmarshaller um = context.createUnmarshaller();
		
		try{
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); 
	        Schema schema = sf.newSchema(new File("./src/main/xsd/todolist-v2.xsd")); 
	        um.setSchema(schema);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());
		
		Todolist h = (Todolist) um.unmarshal(in);
		
		for(ItemType i:h.getItem()){
			i.getId();
			i.getAction();
			i.getDueBy();
		}
	}

}
