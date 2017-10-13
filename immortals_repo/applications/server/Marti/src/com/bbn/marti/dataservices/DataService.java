package com.bbn.marti.dataservices;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataService {
	
	static final Logger logger = LoggerFactory.getLogger(DataService.class);

	public static void main(String[] args) throws IOException {

		if (args.length != 2) {
			System.out.println("Missing one or more parameters. Sample usage: DataService servicename " + BASE_URL + ". Data Service is not started.");
		} else {
			String name = args[0];
			String baseUri = args[1];
			DataService.start(name, baseUri);
		}
	}
	
    public static void start(String name, String baseUrl) throws IOException {

	    	if (dataServiceStatus.compareAndSet(DataServiceStatusValue.STOPPED, DataServiceStatusValue.STARTING)) {
	    		
	    		server = startServer(baseUrl);
	
	        if (server != null && server.isStarted()) {
		        	DataService.baseUrl = baseUrl;
		        	DataService.name = name;
		        	dataServiceStatus.set(DataServiceStatusValue.STARTED);
		    		System.out.println(String.format(DATA_SERVICE_STARTED, baseUrl)); //keep system.out for cli use
		    		logger.info(String.format(DATA_SERVICE_STARTED, baseUrl));
		    	} else {
		    		if (server != null) {
		    			forceShutdown();
		    		} else {
		    			System.out.println(DATA_SERVICE_COULD_NOT_START);
		    			logger.error(DATA_SERVICE_COULD_NOT_START);
		    			dataServiceStatus.set(DataServiceStatusValue.STOPPED);
		    		}
		    	}
	    	}
    }
    
    public static void shutdown() {

	    	if (dataServiceStatus.compareAndSet(DataServiceStatusValue.STARTED, DataServiceStatusValue.STOPPING)) {
	    		if (server != null) {
	    			try {
		    			server.shutdown();
				    System.out.println(DATA_SERVICE_GRACEFULLY_STOPPED);
		    			logger.info(DATA_SERVICE_GRACEFULLY_STOPPED);
		    			dataServiceStatus.set(DataServiceStatusValue.STOPPED);
	    			} catch (Exception e) {
	    				logger.error("Could not gracefully shutdown data services; attempting to force shutdown.", e);
	    				forceShutdown();
	    			}
	    		}
	    	}
    }

    private static HttpServer startServer(String baseUrl) {

	    	HttpServer result = null;
	    	
	    	try {
		    	final ResourceConfig rc = new ResourceConfig().packages("com.bbn.marti.dataservices");
		        
		    rc.register(JacksonFeature.class);
	
		    result = GrizzlyHttpServerFactory.createHttpServer(URI.create(baseUrl), rc);
	    	} catch (Exception e) {
	    		logger.error("Unexpected error starting Grizzly server: ", e);
	    	}
	    	
	    	return result;
    }
    
    private static void forceShutdown() {
    	
	    	if (server != null) {
	    		server.shutdownNow();
	    		server = null;
	        System.out.println(DATA_SERVICE_FORCEFULLY_STOPPED);
	    		logger.trace(DATA_SERVICE_FORCEFULLY_STOPPED);
	    		dataServiceStatus.set(DataServiceStatusValue.STOPPED);
	    	}
    }

    public static DataServiceStatusValue getStatus() {
		return dataServiceStatus.get();
	}
	
	public static String getBaseUrl() {
		return baseUrl;
	}
	
	public static String getName() {
		return name;
	}

	private static String baseUrl;
	private static String name;
	
	public static final String BASE_URL = "http://0.0.0.0:10000/takserver";
	private static AtomicReference<DataServiceStatusValue> dataServiceStatus = 
			new AtomicReference<DataServiceStatusValue>(DataServiceStatusValue.STOPPED);
	private static HttpServer server;
	
	private static final String DATA_SERVICE_COULD_NOT_START = String.format("The data service located at: %s could not be started.", BASE_URL);
	private static final String DATA_SERVICE_GRACEFULLY_STOPPED = String.format("The data service located at: %s has gracefully stopped.", BASE_URL);
	private static final String DATA_SERVICE_FORCEFULLY_STOPPED = String.format("The data service located at: %s did not start or stop gracefully and was forced to shutdown.", BASE_URL);
	private static final String DATA_SERVICE_STARTED = "The data service located at: %s is running.";

}
