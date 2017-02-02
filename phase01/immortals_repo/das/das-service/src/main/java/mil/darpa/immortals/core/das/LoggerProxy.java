package mil.darpa.immortals.core.das;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerProxy {
	
	private WebTarget target = null;
	private Client c = null;
	static final Logger logger = LoggerFactory.getLogger(AdaptationStatus.class);

	public static void main(String[] args) throws Exception {
		LoggerProxy p = new LoggerProxy();
		
		for (int x = 0; x < 100; x++) {
			p.sendLogEntry("test" + x);
		}
	}

	public LoggerProxy() throws Exception {
		setup();
    }
	
	private void setup() throws Exception {
    	
        c = ClientBuilder.newClient();

        c.register(JacksonFeature.class);

        target = c.target("http://127.0.0.1:55555");
    }

	public void close() {
		if (c != null) {
			c.close();
		}
	}

	public void sendLogEntry(String logEntry) {
		
		String data = "{\"statusMessage\":" + "\"" + logEntry + "\"}";
		
		try {
			target.path("visualization/dasStatus").request(javax.ws.rs.core.MediaType.TEXT_PLAIN)
					.post(Entity.entity(data, javax.ws.rs.core.MediaType.APPLICATION_JSON));
		} catch (Exception e) {
			logger.error("Exception logging through logger proxy:" + e.getMessage()); 
		}
	}

}
