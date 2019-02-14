package mil.darpa.immortals.flitcons;


import mil.darpa.immortals.flitcons.datastores.LogData;
import mil.darpa.immortals.flitcons.datastores.orientdb.OrientdbDirectDauInventoryValidator;
import mil.darpa.immortals.flitcons.datastores.xml.OrientdbDauInventoryValidator;
import mil.darpa.immortals.flitcons.datastores.xml.XmlPreprocessor;
import mil.darpa.immortals.flitcons.datatypes.HierarchicalData;
import mil.darpa.immortals.flitcons.datatypes.ScenarioData;
import mil.darpa.immortals.flitcons.solvers.dsl.DSLInterchangeFormat;
import mil.darpa.immortals.flitcons.solvers.dsl.DslSolver;
import mil.darpa.immortals.schemaevolution.ChallengeProblemBridge;
import mil.darpa.immortals.schemaevolution.TerminalStatus;
import picocli.CommandLine;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;


public class Main implements Callable<Void> {

	@CommandLine.Option(names = {"--validate-dau-inventory"}, description = "Validates the DAU Inventory")
	private boolean doValidateDauInventory = false;

	@CommandLine.Option(names = {"--test"}, description = "Testing")
	private boolean doTest = false;

	@CommandLine.Option(names = {"--process-dau-inventory"}, description = "Prepares the DAU inventory for DSL reading")
	private boolean doProcessDauInventory = false;

	private File taggedFile = new File("DauInventory-tagged.xml");
	private File groupMapFile = new File("DauInventory-GroupMapFile.json");


	@Override
	public Void call() throws Exception {
		if (doValidateDauInventory) {
			validateDauInventory();


		} else if (doProcessDauInventory) {
			preprocessDauInventory();

		} else if (doTest) {
			validateDauInventory();
			preprocessDauInventory();
			execute();


		} else {
			execute();
		}

		return null;
	}

	public static void main(String[] args) {
		CommandLine.call(new Main(), args);
	}


	public void preprocessDauInventory() {
		XmlPreprocessor xp = new XmlPreprocessor();
		xp.preprocessDauInventory(taggedFile);
		DummyData.generatePortGroupingMap(taggedFile, groupMapFile);
	}

	public void validateDauInventory() {
		OrientdbDirectDauInventoryValidator v = new OrientdbDirectDauInventoryValidator();
		List<LogData> result0 = v.validateDauInventory(new File("dauinventory-validationresults-orientdb-direct.log"));
		result0 = LogData.createSortedLogDataList(result0);

		QueryHelper qh = new QueryHelper();
		Map<HierarchicalData, Set<HierarchicalData>> resultData = qh.getTestInventoryDauData();
		OrientdbDauInventoryValidator odiv = new OrientdbDauInventoryValidator();
		List<LogData> result1 = odiv.validateDaus(resultData, new File("dauinventory-validationresults-orientdb.log"));
		result1 = LogData.createSortedLogDataList(result1);

		if (result0.size() != result1.size()) {
			throw new RuntimeException("Bad result length! " + result0.size() + " vs " + result1.size());
		} else {
			for (int i = 0; i < result0.size(); i++) {
				LogData l0 = result0.get(i);
				LogData l1 = result1.get(i);

				if (l0.logType != l1.logType) {
					throw new RuntimeException("Bad log type!");
				}

				if (!l0.message.equals(l1.message)) {
					throw new RuntimeException("Bad message: '" + l0.message + "' vs '" + l1.message + "'!");
				}
			}
		}
	}

	public void execute() {
		ChallengeProblemBridge cpb = new ChallengeProblemBridge();
		String evaluationInstanceIdentifier = UUID.randomUUID().toString();

		try {


			QueryHelper s = new QueryHelper();

			// Gather the raw data from the graph
			ScenarioData faultyConfigurationrawData = s.getRawScenarioData();

			// Produce a massaged set of data that lines up with what the DSL expects
			ScenarioData faultyConfigurationMassagedData = ScenarioDataTransformer.postprocess(faultyConfigurationrawData);

			// Produce the DSLInterchangeFormat data
			DSLInterchangeFormat faultyConfigurationData = DSLInterchangeFormat.createFromScenarioData(faultyConfigurationMassagedData);


//		 Load it into the DSL solver and obtain a solution
			DslSolver ds = new DslSolver();
			ds.loadData(faultyConfigurationData);
			DSLInterchangeFormat solution = ds.solve();

			try {
				cpb.postResultsJson(evaluationInstanceIdentifier, TerminalStatus.AdaptationSuccessful, "{ \"Details\": \"This is a mock result. As nothing has actually bee fixed, no changes should be expected to the input graph.\" }");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		} catch (Exception e) {
			try {
				System.err.println(e.getMessage());
				e.printStackTrace();
				cpb.postError(evaluationInstanceIdentifier, e.getMessage(), null);
			} catch (Exception e2) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				System.err.println();
				System.err.println();
				System.out.println();
				System.out.println();
				System.err.println(e2.getMessage());
				e2.printStackTrace();
			}
			System.exit(-1);
		}
	}
}
