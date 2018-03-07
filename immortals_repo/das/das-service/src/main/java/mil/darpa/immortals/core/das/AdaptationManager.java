package mil.darpa.immortals.core.das;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.das.adaptationmodules.IAdaptationModule;
import mil.darpa.immortals.core.das.adaptationmodules.schemaevolution.SchemaEvolutionAdapter;
import mil.darpa.immortals.core.das.sparql.SparqlQuery;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;

public class AdaptationManager {

	static final Logger logger = LoggerFactory.getLogger(AdaptationManager.class);
	private List<IAdaptationModule> adaptationModules = new ArrayList<>();

	static {
		instance = new AdaptationManager();
	}
		
	private AdaptationManager() {
		
		adaptationModules.add(new SchemaEvolutionAdapter());
	}
	
	public static AdaptationManager getInstance() {
		return instance;
	}
			
	public void triggerAdaptation(DasAdaptationContext dac) {
		
		for (IAdaptationModule am : adaptationModules) {
			try {
				if (am.isApplicable(dac)) {
					am.apply(dac);
				}
			} catch (Exception e) {
				ImmortalsErrorHandler.reportFatalError("Unexpected error with adaptation module: " + am.getClass().toString() + ": " + e.getMessage());
				break;
			}
		}
	}
	
	public String initialize() throws IOException {
		
		String graphUri = null;
		
		SimpleKnowledgeRepoClient client = 
                new SimpleKnowledgeRepoClient();
        
		graphUri = client.ingest();
		
		return graphUri;
        
	}

	public String initializeKnowledgeRepo() {

		String byteCodeURI = null;
		Path ingestionPath = ImmortalsConfig.getInstance().globals.getTtlIngestionDirectory();
		
		try {
			repositoryService = ClientBuilder.newClient(new ClientConfig()
					.register(JacksonFeature.class))
					.target(REPOSITORY_SERVICE_CONTEXT_ROOT);
	        byteCodeURI = repositoryService.path("ingest").request().post(
	        		Entity.entity(ingestionPath.toString(), MediaType.TEXT_PLAIN), String.class);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
        if (byteCodeURI != null && byteCodeURI.trim().length() > 0) {
        	byteCodeURI = SparqlQuery.FUSEKI_DATA_ENDPOINT + byteCodeURI;
        }
        
        return byteCodeURI;
        
	}

	private static WebTarget repositoryService;
	private static AdaptationManager instance;
	private static final String REPOSITORY_SERVICE_CONTEXT_ROOT = "http://localhost:9999/krs/";	

}
