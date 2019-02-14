package com.securboration.demo;

import javax.xml.bind.JAXBException;

public abstract class Server {
	
	public void doSomething(String xml) throws JAXBException {
		System.out.printf("%s received an xml message: %s\n",this.getClass().getSimpleName(),xml);
		
		doSomethingInternal(xml);
	}
	
	protected abstract void doSomethingInternal(String xml) throws JAXBException;

}
