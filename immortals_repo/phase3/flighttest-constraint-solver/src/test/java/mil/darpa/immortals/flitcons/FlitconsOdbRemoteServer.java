package mil.darpa.immortals.flitcons;

import org.testng.Assert;

import javax.annotation.Nonnull;

public class FlitconsOdbRemoteServer extends AbstractFlitconsOdbServer {

	private String host;
	private int port;

	public FlitconsOdbRemoteServer(@Nonnull FlitconsTestScenario scenario, @Nonnull String host) {
		super(scenario);
		this.host = host;
		this.port = 2424;
	}

	public FlitconsOdbRemoteServer(@Nonnull FlitconsTestScenario scenario, int port) {
		super(scenario);
		this.host = "127.0.0.1";
		this.port = port;
	}

	public FlitconsOdbRemoteServer(@Nonnull FlitconsTestScenario scenario, @Nonnull String host, int port) {
		super(scenario);
		this.host = host;
		this.port = port;
	}

	@Override
	public synchronized void init() {
		try {
			super.init("remote", host, port);
		} catch (Exception e) {
			Assert.fail("Unexpected exception starting server!", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized void shutdown() {
	}
}
