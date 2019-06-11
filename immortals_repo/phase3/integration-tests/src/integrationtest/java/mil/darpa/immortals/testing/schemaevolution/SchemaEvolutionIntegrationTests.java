package mil.darpa.immortals.testing.schemaevolution;

import mil.darpa.immortals.testing.tools.TestScenarioRunner;
import org.testng.annotations.Test;

public class SchemaEvolutionIntegrationTests {

	@Test
	public void basicTest() {
		TestScenarioRunner.runScenario6Test("s6k");
	}
}
