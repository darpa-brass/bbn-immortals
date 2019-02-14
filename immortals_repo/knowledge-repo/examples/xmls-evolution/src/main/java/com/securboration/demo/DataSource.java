package com.securboration.demo;

import javax.xml.bind.JAXBException;

public abstract class DataSource {
	
	public abstract String generateData() throws JAXBException;

}
