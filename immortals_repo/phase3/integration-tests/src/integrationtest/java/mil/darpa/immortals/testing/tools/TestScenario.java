package mil.darpa.immortals.testing.tools;

import com.google.gson.Gson;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TestScenario {

	private final static String DB_NAME_PREFIX = "IMMORTALS_";


	private static Map<String, TestScenario> scenario5TestScenarios;

	private static Map<String, TestScenario> scenario6TestScenarios;

	private static class TestScenarios {
		public final Set<TestScenario> scenarios;

		public TestScenarios(@Nonnull Set<TestScenario> scenarios) {
			this.scenarios = scenarios;
		}
	}

	private static void init() {
		TestScenarios s5 = new Gson().fromJson(new InputStreamReader(TestScenario.class.getClassLoader().getResourceAsStream("s5_scenarios.json")), TestScenarios.class);
		scenario5TestScenarios = s5.scenarios.stream().peek(x -> x.scenarioType = "Scenario5").collect(Collectors.toMap(x -> x.shortName, x -> x));
		TestScenarios s6 = new Gson().fromJson(new InputStreamReader(TestScenario.class.getClassLoader().getResourceAsStream("s6_scenarios.json")), TestScenarios.class);
		scenario6TestScenarios = s6.scenarios.stream().peek(x -> x.scenarioType = "Scenario6").collect(Collectors.toMap(x -> x.shortName, x -> x));
	}

	public static TestScenario getScenario5TestScenario(@Nonnull String shortName) {
		init();
		return scenario5TestScenarios.get(shortName);
	}

	public static TestScenario getScenario6TestScenario(@Nonnull String shortName) {
		init();
		return scenario6TestScenarios.get(shortName);
	}

	private final String shortName;
	private final String prettyName;
	private String scenarioType;
	private final String xmlInventoryPath;
	private final String xmlMdlrootInputPath;
	private final String jsonInputPath;
	private final LinkedList<String> expectedStatusSequence;


	public TestScenario(@Nonnull String shortName, @Nonnull String prettyName, @Nonnull String scenarioType,
	                    @Nullable String xmlInventoryPath, @Nullable String xmlMdlrootInputPath,
	                    @Nullable String jsonInputPath, @Nonnull List<String> expectedStatusSequence) {
		this.shortName = shortName;
		this.prettyName = prettyName;
		this.scenarioType = scenarioType;
		this.xmlInventoryPath = xmlInventoryPath;
		this.xmlMdlrootInputPath = xmlMdlrootInputPath;
		this.jsonInputPath = jsonInputPath;
		this.expectedStatusSequence = new LinkedList<>(expectedStatusSequence);
	}

	public File getBackupFile() {
		return ProvidedTestingData.getTestDatabaseDirectory().resolve(this.shortName + "-backup.zip").toFile();
	}

	public String getDbName() {
		return DB_NAME_PREFIX + shortName;
	}

	public String getShortName() {
		return shortName;
	}

	public String getPrettyName() {
		return prettyName;
	}

	public String getScenarioType() {
		return scenarioType;
	}

	public String getXmlInventoryPath() {
		return xmlInventoryPath;
	}

	public String getXmlMdlrootInputPath() {
		return xmlMdlrootInputPath;
	}

	public String getJsonInputPath() {
		return jsonInputPath;
	}

	public List<String> getExpectedStatusSequence() {
		return expectedStatusSequence;
	}
}
