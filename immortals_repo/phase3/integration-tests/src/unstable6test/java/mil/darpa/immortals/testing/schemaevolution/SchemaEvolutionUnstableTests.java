package mil.darpa.immortals.testing.schemaevolution;

import mil.darpa.immortals.orientdbserver.JarTestScenarioRunner;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SchemaEvolutionUnstableTests {

	@BeforeClass
	public void init() {
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}

	@Test
	public void s6_v19_embedded_addTest() {
		JarTestScenarioRunner.createScenario6Runner("s6_v19_embedded_add").execute();
	}

	@Test
	public void s6_v19_embedded_moveTest() {
		JarTestScenarioRunner.createScenario6Runner("s6_v19_embedded_move").execute();
	}

	@Test
	public void s6_v19_embedded_move2Test() {
		JarTestScenarioRunner.createScenario6Runner("s6_v19_embedded_move2").execute();
	}

	@Test
	public void s6_v19_embedded_removeTest() {
		JarTestScenarioRunner.createScenario6Runner("s6_v19_embedded_remove").execute();
	}

	@Test
	public void s6_v19_embedded_renameTest() {
		JarTestScenarioRunner.createScenario6Runner("s6_v19_embedded_rename").execute();
	}

	@Test
	public void s6_v19_embedded_rename2Test() {
		JarTestScenarioRunner.createScenario6Runner("s6_v19_embedded_rename2").execute();
	}


	@Test
	public void s6_bbn_add_complex_type_embeddedTest() {
		JarTestScenarioRunner.createScenario6Runner("s6_bbn_add_complex_type").execute();
	}

	@Test
	public void s6_bbn_rename_move_attribute_up_add_rename_enum_type_embeddedTest() {
		JarTestScenarioRunner.createScenario6Runner("s6_bbn_rename_move_attribute_up_add_rename_enum_type").execute();
	}

	@Test
	public void s6_bbn_move_connectors_up_level_embeddedTest() {
		JarTestScenarioRunner.createScenario6Runner("s6_bbn_move_connectors_up_level").execute();
	}

	@Test
	public void s6_bbn_remove_connectors_from_all_embeddedTest() {
		JarTestScenarioRunner.createScenario6Runner("s6_bbn_remove_connectors_from_all").execute();
	}

	@Test
	public void s6_bbn_remove_connectors_from_ports_embeddedTest() {
		JarTestScenarioRunner.createScenario6Runner("s6_bbn_remove_connectors_from_ports").execute();
	}
}
