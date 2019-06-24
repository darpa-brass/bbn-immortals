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
}
