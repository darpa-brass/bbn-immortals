package mil.darpa.immortals.flitcons;

import org.testng.annotations.Test;

public class SanityTests {

	@Test
	public void testServerWaitForReady() {
		EmbeddedTestScenario5Runner.createScenario5Runner("s5").execute(4000);
	}

	@Test
	public void testServerReady() {
		EmbeddedTestScenario5Runner.createScenario5Runner("s5").execute();
	}

	@Test
	public void testServerSequential() {
		EmbeddedTestScenario5Runner.createScenario5Runner("s5s").execute();
	}
}
