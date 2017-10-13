package mil.darpa.immortals.core.das;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class DAS {

	private DAS() {}

    public static void main(String[] args) throws IOException {
    	
    	if (args.length == 0 || args[0] == null || args[0].trim().length() == 0) {
    		System.out.println("Please initialize the DAS with the location of the immortals root folder.\n");
    		System.out.println("Example: \"/Users/xyz/Documents/Immortals/Workspace/trunk\"\n");
    		System.exit(-1);
    	} else {
    		if (args.length == 2) {
    			if (args[1].equals(AdaptationStatus.IGNORE_PROXY_LOGGING)) {
    				System.getProperties().put(AdaptationStatus.IGNORE_PROXY_LOGGING, "true");
    			}
    		}
	    	initialize(args[0]);
	    	DAS.start();
    	}
    }
    
    public static synchronized void start() throws IOException {

        if (dasStatus == null) {
	    	Thread t = new Thread(new Runnable() {
	    		@Override
	            public void run() {
			    	server = startServer();

			        dasStatus = new DASStatus(DASStatusValue.RUNNING);
			        System.out.println(String.format("DAS has started. Press Enter to stop it...", BASE_URI));
			        try {
						System.in.read();
					} catch (IOException e) {
						e.printStackTrace();
					}
			        server.shutdown();
	    		}
	    	});
	    	t.start();
        }
    }
	
    public static synchronized void shutdown() {
    	if (dasStatus != null) {
    		if (server != null && server.isStarted()) {
    			server.shutdown();
    		}
    	}
    }
    
    private static HttpServer startServer() {

    	final ResourceConfig rc = new ResourceConfig().packages("mil.darpa.immortals.core.das");
        
        rc.register(JacksonFeature.class);

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

	public static synchronized DASStatus getDASStatus() {
		return dasStatus;
	}
	
	public static SUTInformation getSUTInformation() {
		return SUTInformation.getInstance();
	}

	private static void initialize(String immortalsRootFolder) {
		
		configuration.put(CFG_IMMORTALS_ROOT, immortalsRootFolder);
	}
	
	public static String getConfigurationValue(String parameterName) {
		return configuration.get(parameterName);
	}
	
	public static final String BASE_URI = "http://0.0.0.0:8080/bbn/";
	public static final String CFG_IMMORTALS_ROOT = "immortals-root";
	private static DASStatus dasStatus;
	private static HttpServer server;
	private static Map<String, String> configuration = new HashMap<String, String>();
}
