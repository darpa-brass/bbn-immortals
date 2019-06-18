package mil.darpa.immortals.testing.flitcons;

import mil.darpa.immortals.testing.tools.JarTestScenarioRunner;
import org.testng.annotations.Test;

public class FlitconsIntegrationTests {

//	@Test
//	public void testInvalidInputPropagation() {
//
//	}
//
//	@Test
//	public void testUnexpectedErrorPropagation() {
//
//	}

	@Test
	public void basicTest() {
		JarTestScenarioRunner.createScenario5Runner("s5").execute();
	}

//	@Test
//	public void testServerWaitForReady() {
//		runTest(TestScenario.getTestScenario("s5"), false, 4000);
//	}

//	@Test
//	public void testServerReady() {
//		runTest(TestScenario.getTestScenario("s5"), false, null);
//	}

//	@Test
//	public void testServerSequential() {
//		JarTestScenarioRunner.createScenario5Runner("s5s").execute();
//	}
}
