package com.bbn.marti.dataservices;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataServiceProxy {
	
	private WebTarget target = null;
	private DataServiceConfiguration dataServiceConfiguration = null;
	Client c = null;
	static final Logger logger = LoggerFactory.getLogger(DataServiceProxy.class);

	public DataServiceProxy(DataServiceConfiguration dataServiceConfiguration) throws Exception {
		setup(dataServiceConfiguration);
    }
	
	private void setup(DataServiceConfiguration dataServiceConfiguration) throws Exception {
    	
        c = ClientBuilder.newClient();

        c.register(JacksonFeature.class);

        target = c.target(dataServiceConfiguration.getBaseUrl());
        
        this.dataServiceConfiguration = dataServiceConfiguration;

    }

	public void close() {
		if (c != null) {
			c.close();
		}
	}

	public DataServiceConfiguration getDataServiceConfiguration() {
		return dataServiceConfiguration;
	}

	public String sendEchoTest(String echoString) throws Exception {

		String response = null;
		
		try {
			response = target.path("dataservices/echoTest").queryParam("echoString", echoString).request().get(String.class);
		} catch (Exception e) {
			throw new Exception("Unexpected error invoking REST endpoint: dataservices/test located: " + dataServiceConfiguration.getBaseUrl(), e);
		}
		
		return response;
	}
}
