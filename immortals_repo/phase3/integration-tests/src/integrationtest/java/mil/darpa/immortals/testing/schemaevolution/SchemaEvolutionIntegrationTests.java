package mil.darpa.immortals.testing.schemaevolution;

import mil.darpa.immortals.testing.tools.TestScenarioRunner;
import org.testng.annotations.Test;

public class SchemaEvolutionIntegrationTests {

//	@Test
	public void s6_12to10() {
		TestScenarioRunner.runScenario6Test("s6_12to10");
	}

	@Test
	public void s6_16to19() {
		TestScenarioRunner.runScenario6Test("s6_16to19");
	}

	@Test
	public void s6_17to19() {
		TestScenarioRunner.runScenario6Test("s6_17to19");
	}

//	@Test
	public void s6_7to14() {
		TestScenarioRunner.runScenario6Test("s6_7to14");
	}
}
