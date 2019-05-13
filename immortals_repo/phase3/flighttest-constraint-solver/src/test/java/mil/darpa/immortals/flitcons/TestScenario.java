package mil.darpa.immortals.flitcons;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class TestScenario {


	private static final Path databaseBackupDir = Paths.get("test_databases").toAbsolutePath();

	private static final Map<String, TestScenario> testScenarios;

	private static class TestScenarios {
		public final Set<TestScenario> scenarios;

		public TestScenarios(@Nonnull Set<TestScenario> scenarios) {
			this.scenarios = scenarios;
		}
	}

	static {
		TestScenarios s = Utils.getGson().fromJson(new InputStreamReader(TestScenario.class.getClassLoader().getResourceAsStream("scenarios.json")), TestScenarios.class);
		testScenarios = s.scenarios.stream().collect(Collectors.toMap(x -> x.shortName, x -> x));
	}

	public static TreeSet<String> getTestScenarioShortNames() {
		return new TreeSet<>(testScenarios.keySet());
	}

	public static Map<String, TestScenario> getTestScenarios() {
		return testScenarios;
	}

	public static TestScenario getTestScenario(@Nonnull String shortName) {
		return testScenarios.get(shortName);
	}


	private final String shortName;
	private final String prettyName;
	private final String scenarioType;
	private final String dbName;
	private final String xmlInventoryPath;
	private final String xmlMdlrootInputPath;
	private final String jsonInputPath;
	private final LinkedList<String> expectedStatusSequence;


	public TestScenario(@Nonnull String shortName, @Nonnull String prettyName, @Nonnull String scenarioType,
	                    @Nonnull String dbName, @Nonnull String xmlInventoryPath, @Nonnull String xmlMdlrootInputPath,
	                    @Nonnull String jsonInputPath, @Nonnull List<String> expectedStatusSequence) {
		this.shortName = shortName;
		this.prettyName = prettyName;
		this.scenarioType = scenarioType;
		this.dbName = dbName;
		this.xmlInventoryPath = xmlInventoryPath;
		this.xmlMdlrootInputPath = xmlMdlrootInputPath;
		this.jsonInputPath = jsonInputPath;
		this.expectedStatusSequence = new LinkedList<>(expectedStatusSequence);
	}

	public File getBackupFile() {
		if (!Files.exists(databaseBackupDir)) {
			try {
				Files.createDirectory(databaseBackupDir);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return databaseBackupDir.resolve(this.shortName + "-backup.zip").toFile();
	}

	public String getDbName() {
		return dbName;
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
