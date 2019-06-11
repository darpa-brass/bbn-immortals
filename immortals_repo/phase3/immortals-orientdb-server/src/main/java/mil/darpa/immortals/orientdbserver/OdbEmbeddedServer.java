package mil.darpa.immortals.orientdbserver;

import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.config.OServerConfiguration;
import com.orientechnologies.orient.server.config.OServerConfigurationManager;
import com.orientechnologies.orient.server.config.OServerNetworkConfiguration;
import com.orientechnologies.orient.server.config.OServerUserConfiguration;
import com.orientechnologies.orient.server.network.protocol.binary.ONetworkProtocolBinary;

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
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void shutdown() {
		if (server != null) {
			server.shutdown();
		}
	}

	public synchronized void waitForShutdown() {
		server.waitForShutdown();
	}
}
