package mil.darpa.immortals.testing.tools;

import mil.darpa.immortals.schemaevolution.ProvidedData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProvidedTestingData {

	private static final String JARGS_EXTERNAL_SERVER_PORT = "mil.darpa.immortals.flitcons.test.server.external.port";
	private static final String JARGS_EXTERNAL_SERVER_HOST = "mil.darpa.immortals.flitcons.test.server.external.host";
	private static final String JARGS_SUT_START_SCRIPT = "mil.darpa.immoratls.test.sut.script";
	private static final String JARGS_TEST_DATABASE_BACKUP_DIR = "mil.darpa.immortals.test.test_databases.dir";

	public static Integer getOdbExternalServerPort() {
		String odbExternalServerPortString = System.getProperty(JARGS_EXTERNAL_SERVER_PORT);
		if (odbExternalServerPortString == null) {
			return null;
		} else {
			return Integer.parseInt(odbExternalServerPortString);
		}
	}

	public static String getOdbExternalServerHost() {
		return System.getProperty(JARGS_EXTERNAL_SERVER_HOST);
	}

	public static Path getTestDatabaseDirectory() {
		Path testDatabaseDirectory;
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
			testDatabaseDirectory = p;
		} else {
			testDatabaseDirectory = Paths.get(databaseBackupDirProp);
			if (!Files.exists(testDatabaseDirectory)) {
				throw new RuntimeException("Provided testDatabaseDirectory '" + testDatabaseDirectory + "' does not exist!");
			}
		}
		return testDatabaseDirectory;
	}

	public static String getSutStartupScript() {
		String sutStartupScript;
		sutStartupScript = System.getProperty(JARGS_SUT_START_SCRIPT);
		if (sutStartupScript == null) {
			throw new RuntimeException("sutStartupScript must be set!");
		} else if (!new File(sutStartupScript).exists()) {
			throw new RuntimeException("sutStartupScript '" + sutStartupScript + "' does not exist!");
		}
		return sutStartupScript;
	}

	static {
		System.out.println("---------------------------------INIT VARIABLES---------------------------------");
		System.out.println("testDatabaseDirectory='" + getTestDatabaseDirectory() + "'");
		System.out.println("sutStartupScript='" + getSutStartupScript() + "'");
		System.out.println("odbExternalServerPort='" + (getOdbExternalServerPort() == null ? "<NONE>" : getOdbExternalServerPort()));
		System.out.println("odbExternalServerHost='" + (getOdbExternalServerHost() == null ? "<NONE>" : getOdbExternalServerHost()));
		System.out.println("--------------------------------------------------------------------------------");
	}

}
