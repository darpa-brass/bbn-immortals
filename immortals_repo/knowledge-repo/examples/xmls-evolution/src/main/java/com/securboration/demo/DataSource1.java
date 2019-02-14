package com.securboration.demo;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.securboration.generated.v1.ActionType;
import com.securboration.generated.v1.ItemType;
import com.securboration.generated.v1.ObjectFactory;
import com.securboration.generated.v1.Todolist;

public class DataSource1 extends DataSource{
	
	private final ObjectFactory factory = new ObjectFactory();

	@Override
	public String generateData() throws JAXBException {
		Todolist document = factory.createTodolist();
		
		for(int i=0;i<10;i++) {
			ItemType item = factory.createItemType();
			
			item.setId(i);
			item.setDescription("an item in a TODOlist");
			item.setName("TODOlist item " + i);
			item.setDueBy(System.currentTimeMillis());
			item.setAction(ActionType.SOMEDAY_MAYBE_DEFER);
			
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
