package mil.darpa.immortals.core.das.restendpoints;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import mil.darpa.immortals.core.das.AdaptationManager;
import mil.darpa.immortals.core.das.AdaptationStatus;
import mil.darpa.immortals.core.das.AdaptationStatusValue;
import mil.darpa.immortals.core.das.DAS;
import mil.darpa.immortals.core.das.DASStatus;
import mil.darpa.immortals.core.das.DASStatusValue;
import mil.darpa.immortals.core.das.SUTInformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("das")
public class DASEndpoint {

	static final Logger logger = LoggerFactory.getLogger(DASEndpoint.class);
	
	@GET
	@Path("/test")
	@Produces(MediaType.TEXT_PLAIN)
	public String test() {
		
		logger.trace("/test REST endpoint invoked.");

		return "Basic REST services working with DAS.";
	}
	
    @GET
    @Path("/sut-about")
    @Produces(MediaType.APPLICATION_JSON)
    public SUTInformation getAbout() {
    	
		logger.trace("/sut-about REST endpoint invoked.");
		
    	return DAS.getSUTInformation();
    }
    
    @GET
    @Path("/das-status")
    @Produces(MediaType.APPLICATION_JSON)
    public DASStatus getDASStatus() {    	

		logger.trace("/das-status REST endpoint invoked.");

    	return DAS.getDASStatus();
    }
    
    @POST
    @Path("/deployment-model")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public AdaptationStatus triggerAdaptation(String deploymentModelRdf) {

		logger.error("/deployment-model REST endpoint invoked.");

    	AdaptationStatus result = null;

    	try {
	    	if (DAS.getDASStatus().getStatus() != DASStatusValue.RUNNING) {
	    		logger.trace("DAS not available (another request may be in progress).");
	    		result =  new AdaptationStatus(AdaptationStatusValue.ERROR, 
	    				"DAS is not available (another request may currently be in progress)");
	    	} else {
	    		logger.trace("DAS beginning adaptation.");

	    		DAS.getDASStatus().setStatus(DASStatusValue.OFFLINE_ADAPTATION);
		    	
		    	AdaptationManager am = AdaptationManager.getInstance();
		    	result = am.triggerAdaptation(deploymentModelRdf);
		    	
		    	logger.trace("DAS completed adaptation successfully.");
	    	}
    	} catch (Throwable e) {
    		logger.error("Unexpected error in DAS:", e);

    		result =  new AdaptationStatus(AdaptationStatusValue.ERROR, 
    				"An unexpected error has occurred: " + e.getMessage());    		
    	} finally {
    		DAS.getDASStatus().setStatus(DASStatusValue.RUNNING);
    		result.close();
    	}
    	
    	return result;
    }

}
