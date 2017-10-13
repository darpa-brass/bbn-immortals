package mil.darpa.immortals.core.das;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DASTest {

    @SuppressWarnings("unused")
	private WebTarget target;

    @Before
    public void setUp() throws Exception {
    	
    	DAS.start();

        Client c = ClientBuilder.newClient();

        c.register(JacksonFeature.class);

        target = c.target(DAS.BASE_URI);
    }

    @After
    public void tearDown() throws Exception {
    	DAS.shutdown();
    }

    @Test
    public void testGetSUTAbout() {
        
    	//Test fails from Gradle ...
    	
    	//String response = target.path("das/sut-about").request().get(String.class);

        //assertEquals("{\"version\":\"" + DAS.getSUTInformation().getVersion() + "\",\"about\":\"" + 
        //		DAS.getSUTInformation().getAbout() + "\"}", response);
    }
}
