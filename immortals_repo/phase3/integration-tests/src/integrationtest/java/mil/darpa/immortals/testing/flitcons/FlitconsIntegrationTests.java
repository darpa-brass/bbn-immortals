package mil.darpa.immortals.testing.flitcons;

import mil.darpa.immortals.testing.TestScenarioRunner;
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
		TestScenarioRunner.runScenario5Test("s5");
	}

//	@Test
//	public void testServerWaitForReady() {
//		runTest(TestScenario.getTestScenario("s5"), false, 4000);
//	}
//
//	@Test
//	public void testServerReady() {
//		runTest(TestScenario.getTestScenario("s5"), false, null);
//	}
//
//	@Test
//	public void testServerSequential() {
//		runTest(TestScenario.getTestScenario("s5s"), false, null);
//	}
}
