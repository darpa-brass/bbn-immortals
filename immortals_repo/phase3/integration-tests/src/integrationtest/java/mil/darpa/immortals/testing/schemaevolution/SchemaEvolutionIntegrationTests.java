package mil.darpa.immortals.testing.schemaevolution;

import mil.darpa.immortals.orientdbserver.JarTestScenarioRunner;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SchemaEvolutionIntegrationTests {

	@BeforeClass
	public void init() {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}

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
