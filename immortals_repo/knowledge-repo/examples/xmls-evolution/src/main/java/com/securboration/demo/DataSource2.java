package com.securboration.demo;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.securboration.generated.v2.ActionType;
import com.securboration.generated.v2.ItemType;
import com.securboration.generated.v2.ObjectFactory;
import com.securboration.generated.v2.Todolist;

public class DataSource2 extends DataSource{
	
	private final ObjectFactory factory = new ObjectFactory();

	@Override
	public String generateData() throws JAXBException {
		Todolist document = factory.createTodolist();
		
		for(int i=0;i<10;i++) {
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(new Date());
			XMLGregorianCalendar dueDate = null;
			
			try {
				dueDate =
						DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
			
			ItemType item = factory.createItemType();
			
			item.setId(i);
			item.setDescription("an item in a TODOlist");
			item.setName("TODOlist item " + i);
			item.setAction(ActionType.INCUBATE);
			item.setDueBy(dueDate);
			
			document.getItem().add(item);
		}
		
		JAXBContext context = JAXBContext.newInstance(Todolist.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        OutputStream out = new ByteArrayOutputStream();
        
        m.marshal(document, out);
        
        return out.toString();
	}
	
	

}
