package mil.darpa.immortals.orientdbserver;

import mil.darpa.immortals.schemaevolution.TerminalStatus;
import org.slf4j.bridge.SLF4JBridgeHandler;
import picocli.CommandLine;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class ImmortalsOdbServerMain {

	public static void main(String[] args) {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();

		ImmortalsOdbServerConfiguration config = ImmortalsOdbServerConfiguration.getInstance();
		ImmortalsOdbServerMain server = new ImmortalsOdbServerMain();
		CommandLine.populateCommand(config, args);

		config.validate();
		server.execute();
	}

	private void execute() {
		ImmortalsOdbServerConfiguration config = ImmortalsOdbServerConfiguration.getInstance();
		OdbEmbeddedServer server;

		// First, get all the scenarios that should be regenerated
		List<TestScenario> scenarios = config.getScenariosToRegenerate();
		if (scenarios.size() > 0) {
			server = new OdbEmbeddedServer(scenarios.toArray(new TestScenario[0]));
			// And populate the server without using backups, which will generate new backups
			server.init(config.getDeploymentMode());
			if (!config.keepRunning) {
				server.shutdown();
			}
		}

		scenarios = config.getScenariosToStart();
		if (scenarios.size() > 0) {
			server = new OdbEmbeddedServer(scenarios.toArray(new TestScenario[0]));
			server.init(config.getDeploymentMode());

			for (TestScenario scenario : scenarios) {
				server.setState(scenario, TerminalStatus.ReadyForAdaptation, false);
			}
			server.waitForShutdown();
		}
	}
}
