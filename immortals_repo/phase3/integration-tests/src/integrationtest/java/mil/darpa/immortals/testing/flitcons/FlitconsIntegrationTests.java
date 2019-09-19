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
		JarTestScenarioRunner.createScenario5Runner("bs5eNoThermocoupleExtensions_iThermocoupleExtensions").execute();
	}

	@Test
	public void testServerWaitForReady() {
		JarTestScenarioRunner.createScenario5Runner("bs5eThermocoupleExtensions_iNoThermocoupleExtensions").execute(4000);
	}


	@Test
	public void testServerSequential() {
		JarTestScenarioRunner.createScenario5Runner("bs5eThermocoupleExtensions_iThermocoupleExtensionsSequential").execute();
	}

	@Test
	public void testSwriExample1() {
		JarTestScenarioRunner.createScenario5Runner("s5e01i01").execute();
	}

	@Test
	public void testSwriExample2() {
		JarTestScenarioRunner.createScenario5Runner("s5e02i01").execute();
	}

	@Test
	public void testSwriExample3() {
		JarTestScenarioRunner.createScenario5Runner("s5e03i01").execute();
	}

	@Test
	public void testSwriExample4() {
		JarTestScenarioRunner.createScenario5Runner("s5e04i01").execute();
	}

	@Test
	public void testSwriExample5() {
		JarTestScenarioRunner.createScenario5Runner("s5e05i01").execute();
	}

	@Test
	public void testSwriExample6() {
		JarTestScenarioRunner.createScenario5Runner("s5e06i01").execute();
	}
}
