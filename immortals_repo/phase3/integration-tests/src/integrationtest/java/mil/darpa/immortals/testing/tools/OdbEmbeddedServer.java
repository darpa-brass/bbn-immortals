package mil.darpa.immortals.testing.tools;

import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.network.protocol.binary.ONetworkProtocolBinary;
import org.testng.Assert;

import javax.annotation.Nonnull;

public class OdbEmbeddedServer extends AbstractOdbServer {

	private OServer server;

	public OdbEmbeddedServer(@Nonnull TestScenario scenario) {
		super(scenario);
	}

	@Override
	public synchronized void init() {
		try {
			server = OServerMain.create();
			server.startup(OdbEmbeddedServer.class.getClassLoader().getResourceAsStream("odb_test_cfg.xml"));
			server.activate();

			if (scenario.getScenarioType().equals("Scenario5")) {
				System.out.println("MODE: S5");
				// These scenarios have complex XML graphs that can take a while to load so we will use
				// The databases when available to speed up loading (which is only possible with a plocal connection).
				super.init("plocal", "127.0.0.1", server.getListenerByProtocol(ONetworkProtocolBinary.class).getInboundAddr().getPort());
			} else {
				System.out.println("MODE: S6");
				// Otherwise we will insert stuff manually using a remote connection
				super.init("remote", "127.0.0.1", server.getListenerByProtocol(ONetworkProtocolBinary.class).getInboundAddr().getPort());
			}

		} catch (Exception e) {
			Assert.fail("Unexpected exception starting server!", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void shutdown() {
		if (server != null) {
			server.shutdown();
		}
	}
}
