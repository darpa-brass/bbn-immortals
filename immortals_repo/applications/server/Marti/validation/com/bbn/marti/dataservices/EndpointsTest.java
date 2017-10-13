package com.bbn.marti.dataservices;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EndpointsTest {
	
	private DataServiceProxy proxy = null;

	@Before
	public void setUp() throws Exception {

		DataService.start("test", DataService.BASE_URL);
		
		DataServiceConfiguration config = new DataServiceConfiguration(DataService.BASE_URL);
		
		proxy = new DataServiceProxy(config);
	}

	@After
	public void tearDown() throws Exception {

		DataService.shutdown();

	}

	@Test
	public void test() {

		try {
			final String TEST_STRING = "Echo test.";
	    		String response = proxy.sendEchoTest(TEST_STRING);
	    		assertEquals(TEST_STRING, response);
	    	} catch (Exception e) {
	    		fail();
	    	}
		
	}

}
