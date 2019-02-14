package mil.darpa.immortals.core.das;

import java.net.URI;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

import mil.darpa.immortals.config.DasServiceConfiguration;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.das.knowledgebuilders.building.GradleKnowledgeBuilder;
import mil.darpa.immortals.das.ImmortalsProcessBuilder;

import org.apache.jena.query.ARQ;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DAS {

    static final Logger logger = LoggerFactory.getLogger(DAS.class);

	private DAS() {}

    public static void main(String[] args) throws Exception {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
	    // Necessary for jena to work..
        ARQ.init();
    	
    	if (args.length == 1) {
    		String argument = args[0].trim();
    		if (argument.equalsIgnoreCase(ANALYZE)) {
                //Run through knowledge builders to analyze code; exit DAS when done
                KnowledgeBuilderManager.initialize();
            } else if (argument.equalsIgnoreCase(PERFORM_SCHEMA_ANALYSIS)) {
    		    KnowledgeBuilderManager.performSchemaAnalysis();
    		} else if (argument.equalsIgnoreCase(HELP)) {
    			//Print help and exit
    			System.out.println("An optional parameter may be supplied to perform startup analysis; e.g., 'java DAS --analyze' /n After analysis completes, the DAS exits. "
    					+ "To start DAS keep it running to respond to adaptation triggers, start the DAS without parameters.");
    		} else {
    		}
    	} else if (args.length == 0) {
			//Start DAS and keep running to respond to adaptation triggers
	    	DAS.start();
	    	
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					DAS.shutdown();
				}
			});
    	} else {
    		System.out.println("Invalid arguments; type java DAS --help");
    	}

    }

    public static synchronized void start() throws Exception {
    	
        if (dasStatus.compareAndSet(DASStatusValue.STOPPED, DASStatusValue.STARTING)) {
             
            server = startServer();

            // Create the file
//            java.nio.file.Path rdfPath =
//                    ImmortalsConfig.getInstance().extensions.getProducedTtlOutputDirectory().resolve("deploymentModel.ttl");
//            Files.write(rdfPath, rdf.getBytes());

           
            if (server != null && server.isStarted()) {
                dasStatus.set(DASStatusValue.RUNNING);
                System.out.println(String.format(DAS_STARTED, DAS.BASE_URI)); //keep system.out for cli use
            } else {
                if (server != null) {
                    forceShutdown();
                } else {
                    System.out.println(DAS_COULD_NOT_START);
                    logger.error(DAS_COULD_NOT_START);
                    dasStatus.set(DASStatusValue.STOPPED);
                }
            }
        }
    }

    public static synchronized void shutdown() {
        if (dasStatus.compareAndSet(DASStatusValue.RUNNING, DASStatusValue.STOPPING)) {
            if (server != null) {
                try {
                    server.shutdown();
                    System.out.println(DAS_GRACEFULLY_STOPPED);
                    dasStatus.set(DASStatusValue.STOPPED);
                } catch (Exception e) {
                    logger.error("Could not gracefully shutdown DAS; attempting to force shutdown.", e);
                    forceShutdown();
                }

            }
        }
        
        // Shutdown Processes that are still running
        ImmortalsProcessBuilder.shutdownAllProcesses();
    }

    private static void forceShutdown() {

        if (server != null) {
            server.shutdownNow();
            server = null;
            System.out.println(DAS_FORCEFULLY_STOPPED);
            logger.trace(DAS_FORCEFULLY_STOPPED);
            dasStatus.set(DASStatusValue.STOPPED);
        }
    }


    private static HttpServer startServer() {

        HttpServer result = null;

        try {
            final ResourceConfig rc = new ResourceConfig().packages("mil.darpa.immortals.core.das");

            rc.register(JacksonFeature.class);

            result = GrizzlyHttpServerFactory.createHttpServer(URI.create(

                    BASE_URI), rc);
        } catch (Exception e) {
            logger.error("Unexpected error starting Grizzly server: ", e);
        }

        return result;
    }

    public static DASStatusValue getStatus() {
        return dasStatus.get();
    }

    public static SUTInformation getSUTInformation() {
        return SUTInformation.getInstance();
    }


    private static final DasServiceConfiguration config = ImmortalsConfig.getInstance().dasService;

    public static final String BASE_URI = config.getProtocol() + "://" + config.getUrl() + ":" + config.getPort() + "/bbn/";

    private static AtomicReference<DASStatusValue> dasStatus =
            new AtomicReference<DASStatusValue>(DASStatusValue.STOPPED);

    private static HttpServer server;

    private static final String DAS_COULD_NOT_START = String.format("The DAS service located at: %s could not be started.", BASE_URI);
    private static final String DAS_GRACEFULLY_STOPPED = String.format("The DAS service located at: %s has gracefully stopped.", BASE_URI);
    private static final String DAS_FORCEFULLY_STOPPED = String.format("The DAS service at: %s did not start or stop gracefully and was forced to shutdown.", BASE_URI);
    private static final String DAS_STARTED = "The DAS service located at: %s is running.";
    private static final String ANALYZE = "--ANALYZE";
    private static final String PERFORM_SCHEMA_ANALYSIS = "--PERFORM-SCHEMA-ANALYSIS";
    private static final String HELP = "--HELP";
}
