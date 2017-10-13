package com.bbn.marti.dataservices;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("dataservices")
public class Endpoints {
	
	static final Logger logger = LoggerFactory.getLogger(Endpoints.class);
	
	@GET
	@Path("/echoTest")
	@Produces(MediaType.TEXT_PLAIN)
	public String test(@QueryParam("echoString") String echoString) {
		
		logger.trace("test REST endpoint invoked.");
		
		return echoString;
	}
	
	@POST
	@Path("/shutdown")
	public void shutdown() {
		
		logger.trace("shutdown REST endpoint invoked.");
		
		DataService.shutdown();
	}
	
	@GET
	@Path("/staticqueries/cotEventsForConstantCotType")
	@Produces(MediaType.APPLICATION_JSON)
	public QueryResult cotEventsForConstantCotType(@QueryParam("compareToBaseline") boolean compareToBaseline) {
		
		QueryResult result = new QueryResult();
		
		return result;
		
	}
}
