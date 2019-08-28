package mil.darpa.immortals.testing.flitcons;

import mil.darpa.immortals.orientdbserver.JarTestScenarioRunner;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FlitconsIntegrationTests {

	@BeforeClass
	public void init() {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}

	@Test
	public void testServerReady() {
		JarTestScenarioRunner.createScenario5Runner("s5").execute();
	}

	@Test
	public void testServerWaitForReady() {
		JarTestScenarioRunner.createScenario5Runner("s5").execute(4000);
	}


	@Test
	public void testServerSequential() {
		JarTestScenarioRunner.createScenario5Runner("s5s").execute();
	}

	@Test
	public void testSwriExample1() {
		JarTestScenarioRunner.createScenario5Runner("s5e1").execute();
	}

	@Test
	public void testSwriExample2() {
		JarTestScenarioRunner.createScenario5Runner("s5e2").execute();
//		JarTestScenarioRunner runner = (JarTestScenarioRunner) JarTestScenarioRunner.createScenario5Runner("s5e2");
//		runner.useSimpleSolver = true;
//		runner.execute();
	}
}
