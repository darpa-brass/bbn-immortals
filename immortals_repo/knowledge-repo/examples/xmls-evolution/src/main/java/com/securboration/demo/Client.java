package com.securboration.demo;

import javax.xml.bind.JAXBException;

public class Client {
	
	public Client() {}
	
	public void sendMessage(Server s, String xml) throws JAXBException {
		s.doSomething(xml);
	}

}
