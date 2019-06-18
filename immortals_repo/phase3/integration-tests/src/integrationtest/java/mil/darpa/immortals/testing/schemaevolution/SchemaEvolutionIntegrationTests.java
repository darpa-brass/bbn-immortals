package mil.darpa.immortals.testing.schemaevolution;

import mil.darpa.immortals.testing.tools.JarTestScenarioRunner;
import mil.darpa.immortals.testing.tools.TestScenarioRunner;
import org.testng.annotations.Test;

public class SchemaEvolutionIntegrationTests {

//	@Test
//	public void s6_12to10() {
//		JarTestScenarioRunner.createScenario6Runner("s6_12to10").execute();
//	}

//	@Test
//	public void s6_16to19() {
//		JarTestScenarioRunner.createScenario6Runner("s6_16to19").execute();
//	}

//	@Test
//	public void s6_17to19() {
//		JarTestScenarioRunner.createScenario6Runner("s6_17to19").execute();
//	}

	@Test
	public void s6_7to14() {
		JarTestScenarioRunner.createScenario6Runner("s6_7to14").execute();
	}
}
