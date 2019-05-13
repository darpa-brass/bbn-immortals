package mil.darpa.immortals.flitcons;

import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.network.protocol.binary.ONetworkProtocolBinary;
import org.testng.Assert;

import javax.annotation.Nonnull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static mil.darpa.immortals.schemaevolution.ChallengeProblemBridgeInterface.JARGS_ARTIFACT_DIRECTORY;
import static mil.darpa.immortals.schemaevolution.ChallengeProblemBridgeInterface.JARGS_EVAL_ODB;

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
