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
	public void testMeasurementDuplication() {
		JarTestScenarioRunner.createScenario5Runner("bs5eVariedMeasurements_iSharedMeasurements").execute();
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

	@Test
	public void testSwriExample7() {
		JarTestScenarioRunner.createScenario5Runner("s5e07i01").execute();
	}

	@Test
	public void testSwriExample8() {
		JarTestScenarioRunner.createScenario5Runner("s5e08i01").execute();
	}

	@Test
	public void testSwriExample9() {
		JarTestScenarioRunner.createScenario5Runner("s5e09i01").execute();
	}

	@Test
	public void testSwriExample10() {
		JarTestScenarioRunner.createScenario5Runner("s5e10i01").execute();
	}

	@Test
	public void testSwriExample11() {
		JarTestScenarioRunner.createScenario5Runner("s5e11i01").execute();
	}

	@Test
	public void testSwriExample12() {
		JarTestScenarioRunner.createScenario5Runner("s5e12i01").execute();
	}

	@Test
	public void testSwriExample13() {
		JarTestScenarioRunner.createScenario5Runner("s5e13i01").execute();
	}

	@Test
	public void testSwriExample14() {
		JarTestScenarioRunner.createScenario5Runner("s5e14i01").execute();
	}

	@Test
	public void testSwriExample15() {
		JarTestScenarioRunner.createScenario5Runner("s5e15i01").execute();
	}

	@Test
	public void testSwriExample16() {
		JarTestScenarioRunner.createScenario5Runner("s5e16i01").execute();
	}
}
