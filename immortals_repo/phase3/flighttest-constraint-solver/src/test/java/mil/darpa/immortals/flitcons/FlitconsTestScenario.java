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

public class FlitconsTestScenario {

	public static final String JARGS_TEST_DATABASE_BACKUP_DIR = "mil.darpa.immortals.test.test_databases.dir";

	private final static String DB_NAME_PREFIX = "IMMORTALS_";

	private static final Path databaseBackupDir;

	private static Map<String, FlitconsTestScenario> testScenarios;

	private static class TestScenarios {
		public final Set<FlitconsTestScenario> scenarios;

		public TestScenarios(@Nonnull Set<FlitconsTestScenario> scenarios) {
			this.scenarios = scenarios;
		}
	}

	static {
		String databaseBackupDirProp = System.getProperty(JARGS_TEST_DATABASE_BACKUP_DIR);
		if (databaseBackupDirProp == null) {
			Path p = Paths.get("PRODUCED_TEST_DATABASES");
			if (!Files.exists(p)) {
				try {
					Files.createDirectory(p);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			databaseBackupDir = p;
		} else {
			databaseBackupDir = Paths.get(databaseBackupDirProp);
		}
		TestScenarios s = Utils.getGson().fromJson(new InputStreamReader(FlitconsTestScenario.class.getClassLoader().getResourceAsStream("scenarios.json")), TestScenarios.class);
		testScenarios = s.scenarios.stream().collect(Collectors.toMap(x -> x.shortName, x -> x));
	}

	public static TreeSet<String> getTestScenarioShortNames() {
		return new TreeSet<>(testScenarios.keySet());
	}

	public static Map<String, FlitconsTestScenario> getTestScenarios() {
		return testScenarios;
	}

	public static FlitconsTestScenario getTestScenario(@Nonnull String shortName) {
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


	public FlitconsTestScenario(@Nonnull String shortName, @Nonnull String prettyName, @Nonnull String scenarioType,
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
