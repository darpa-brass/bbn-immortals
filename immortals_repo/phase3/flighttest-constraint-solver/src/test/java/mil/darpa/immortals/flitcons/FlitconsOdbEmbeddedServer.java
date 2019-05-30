package mil.darpa.immortals.flitcons;

import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.network.protocol.binary.ONetworkProtocolBinary;
import org.testng.Assert;

import javax.annotation.Nonnull;

public class FlitconsOdbEmbeddedServer extends AbstractFlitconsOdbServer {

	private OServer server;

	public FlitconsOdbEmbeddedServer(@Nonnull FlitconsTestScenario scenario) {
		super(scenario);
	}

	@Override
	public synchronized void init() {
		try {
			server = OServerMain.create();
			server.startup(FlitconsOdbEmbeddedServer.class.getClassLoader().getResourceAsStream("odb_test_cfg.xml"));
			server.activate();
			super.init("plocal", "127.0.0.1", server.getListenerByProtocol(ONetworkProtocolBinary.class).getInboundAddr().getPort());

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
