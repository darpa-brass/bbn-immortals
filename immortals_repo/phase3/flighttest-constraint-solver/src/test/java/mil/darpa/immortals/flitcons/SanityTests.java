package mil.darpa.immortals.flitcons;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SanityTests {

	@BeforeClass
	public void init() {
			SLF4JBridgeHandler.removeHandlersForRootLogger();
			SLF4JBridgeHandler.install();
	}

	@Test
	public void measurementDuplicationTest() {
		EmbeddedTestScenario5Runner.createScenario5Runner("bs5eVariedMeasurements_iSharedMeasurements").execute();
	}

	@Test
	public void testServerWaitForReady() {
		EmbeddedTestScenario5Runner.createScenario5Runner("bs5eThermocoupleExtensions_iThermocoupleExtensions").execute(4000);
	}

	@Test
	public void testServerReady() {
		EmbeddedTestScenario5Runner.createScenario5Runner("bs5eThermocoupleExtensions_iThermocoupleExtensions").execute();
	}

	@Test
	public void testServerSequential() {
		EmbeddedTestScenario5Runner.createScenario5Runner("bs5eThermocoupleExtensions_iThermocoupleExtensionsSequential").execute();
	}

	@Test
	public void testSwriExample1() {
		EmbeddedTestScenario5Runner.createScenario5Runner("s5e01i01").execute();
	}

	@Test
	public void testSwriExample2() {
		EmbeddedTestScenario5Runner.createScenario5Runner("s5e02i01").execute();
	}
}
