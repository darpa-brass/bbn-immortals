package mil.darpa.immortals.schemaevolution;

import com.google.gson.Gson;
import com.orientechnologies.orient.core.exception.OConfigurationException;
import mil.darpa.immortals.schemaevolution.datatypes.InputData;
import mil.darpa.immortals.schemaevolution.datatypes.KnownMdlSchemaVersions;
import mil.darpa.immortals.schemaevolution.datatypes.OutputData;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;

public class BBNIntegrationTests {

	@Test
	public void scenario6BasicTest() {
		try {
			Gson gson = new Gson();

			ChallengeProblemBridge cpb;
			System.setProperty(ChallengeProblemBridge.JARGS_EVAL_ODB, "remote:127.0.0.1/IMMORTALS_TEST-SCENARIO_6-KNOWN_SCHEMA");
			System.setProperty(ChallengeProblemBridge.JARGS_PERS_ODB, "remote:127.0.0.1/BBNPersistent");

			cpb = new ChallengeProblemBridge();

			String evaluationInstanceIdentifier = UUID.randomUUID().toString();

			String configurationJson = cpb.getConfigurationJson(evaluationInstanceIdentifier);
			InputData configuration = gson.fromJson(configurationJson, InputData.class);

			Assert.assertEquals(configuration.initialMdlVersion, KnownMdlSchemaVersions.V0_8_17);
			Assert.assertEquals(configuration.updatedMdlVersion, KnownMdlSchemaVersions.V0_8_19);
			Assert.assertNull(configuration.updatedMdlSchema);

			OutputData results = new OutputData(evaluationInstanceIdentifier, configuration, "Pass", "tookTooLong", "ILikeCheese");

			String resultsJson = gson.toJson(results);
			cpb.postResultsJson(evaluationInstanceIdentifier, TerminalStatus.AdaptationSuccessful, resultsJson);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void scenario6AdvancedTest() {
		try {
			Gson gson = new Gson();

			ChallengeProblemBridge cpb;
			System.setProperty(ChallengeProblemBridge.JARGS_EVAL_ODB, "remote:127.0.0.1/IMMORTALS_TEST-SCENARIO_6-UNKNOWN_SCHEMA");
			System.setProperty(ChallengeProblemBridge.JARGS_PERS_ODB, "remote:127.0.0.1/BBNPersistent");

			cpb = new ChallengeProblemBridge();

			String evaluationInstanceIdentifier = UUID.randomUUID().toString();

			String configurationJson = cpb.getConfigurationJson(evaluationInstanceIdentifier);
			InputData configuration = gson.fromJson(configurationJson, InputData.class);

			Assert.assertEquals(configuration.initialMdlVersion, KnownMdlSchemaVersions.V0_8_17);
			Assert.assertNull(configuration.updatedMdlVersion);
			Assert.assertNotNull(configuration.updatedMdlSchema);

			OutputData results = new OutputData(evaluationInstanceIdentifier, configuration, "Pass", "tookTooLong", "ILikeCheese");

			String resultsJson = gson.toJson(results);
			cpb.postResultsJson(evaluationInstanceIdentifier, TerminalStatus.AdaptationSuccessful, resultsJson);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void scenario6ErrorTest() {
		try {
			ChallengeProblemBridge cpb;
			System.setProperty(ChallengeProblemBridge.JARGS_EVAL_ODB, "remote:127.0.0.1/IMMORTALS_TEST-SCENARIO_6-UNKNOWN_SCHEMA");
			System.setProperty(ChallengeProblemBridge.JARGS_PERS_ODB, "remote:127.0.0.1/BBNPersistent");

			cpb = new ChallengeProblemBridge();

			String evaluationInstanceIdentifier = UUID.randomUUID().toString();

			cpb.postError(evaluationInstanceIdentifier, "Error 0", null);

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void scenario6ErrorTestBadEvalServer() {
		boolean configExceptionHit = false;

		try {
			ChallengeProblemBridge cpb;
			System.setProperty(ChallengeProblemBridge.JARGS_EVAL_ODB, "remote:127.0.0.1/IMMORTALS_TEST-SCENARIO_6-UNKNOWN_SCHEMAx");
			System.setProperty(ChallengeProblemBridge.JARGS_PERS_ODB, "remote:127.0.0.1/BBNPersistent");

			cpb = new ChallengeProblemBridge();

			String evaluationInstanceIdentifier = UUID.randomUUID().toString();

			cpb.postError(evaluationInstanceIdentifier, "Error 0", null);
		} catch (OConfigurationException e) {
			Assert.assertTrue(e.getMessage().contains("IMMORTALS_TEST-SCENARIO_6-UNKNOWN_SCHEMAx"));
			configExceptionHit = true;

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		Assert.assertTrue(configExceptionHit);
	}

	@Test
	public void scenario6ErrorTestBadPersistentServer() {
		boolean configExceptionHit = false;

		try {
			ChallengeProblemBridge cpb;
			System.setProperty(ChallengeProblemBridge.JARGS_EVAL_ODB, "remote:127.0.0.1/IMMORTALS_TEST-SCENARIO_6-UNKNOWN_SCHEMA");
			System.setProperty(ChallengeProblemBridge.JARGS_PERS_ODB, "remote:127.0.0.1/BBNPersistentx");

			cpb = new ChallengeProblemBridge();

			String evaluationInstanceIdentifier = UUID.randomUUID().toString();

			cpb.postError(evaluationInstanceIdentifier, "Error 0", null);
		} catch (OConfigurationException e) {
			Assert.assertTrue(e.getMessage().contains("BBNPersistentx"));
			configExceptionHit = true;

		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.getMessage());
		}
		Assert.assertTrue(configExceptionHit);
	}

}
