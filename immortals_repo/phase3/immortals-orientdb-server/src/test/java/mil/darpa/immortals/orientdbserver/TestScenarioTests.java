package mil.darpa.immortals.orientdbserver;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestScenarioTests {

	@Test
	public void generalTest() {
		TestScenario s5 = TestScenarios.getTestScenario("s5DummyTest5BackupExists");
		Assert.assertNotNull(s5.getBackupInputStream());
		Assert.assertNull(s5.getInputJsonData());
		// Not necessary since Python is used to deal with raw XML files
//		Assert.assertNull(s5.getXmlInventoryPath());
//		Assert.assertNull(s5.getXmlMdlrootInputPath());

		s5 = TestScenarios.getTestScenario("s5DummyTest5BackupDoesNotExist");
		Assert.assertNull(s5.getBackupInputStream());
		Assert.assertNull(s5.getInputJsonData());
//		Assert.assertNotNull(s5.getXmlInventoryPath());
//		Assert.assertNotNull(s5.getXmlMdlrootInputPath());

		TestScenario s6 = TestScenarios.getTestScenario("s6DummyJsonInputInResources");
		Assert.assertNotNull(s6.getInputJsonData());

		s6 = TestScenarios.getTestScenario("s6DummyJsonInputInResources");
		Assert.assertNotNull(s6.getInputJsonData());

		s6 = TestScenarios.getTestScenario("s6DummyJsonFileAndResourceExists");
		Assert.assertNotNull(s6.getInputJsonData());
	}
}
