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

	@Test
	public void s6_17to19() {
		JarTestScenarioRunner.createScenario6Runner("s6_17to19").execute();
	}

	@Test
	public void s6_v19_embedded_add() {
		JarTestScenarioRunner.createScenario6Runner("s6_v19_embedded_add").execute();
	}

	@Test
	public void s6_v19_embedded_move() {
		JarTestScenarioRunner.createScenario6Runner("s6_v19_embedded_move").execute();
	}

	@Test
	public void s6_v19_embedded_move2() {
		JarTestScenarioRunner.createScenario6Runner("s6_v19_embedded_move2").execute();
	}

	@Test
	public void s6_v19_embedded_remove() {
		JarTestScenarioRunner.createScenario6Runner("s6_v19_embedded_remove").execute();
	}

	@Test
	public void s6_v19_embedded_rename() {
		JarTestScenarioRunner.createScenario6Runner("s6_v19_embedded_rename").execute(); }
}
