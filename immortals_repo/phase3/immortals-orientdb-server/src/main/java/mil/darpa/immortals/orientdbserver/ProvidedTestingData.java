package mil.darpa.immortals.orientdbserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ProvidedTestingData {

	private static final String JARGS_IMMORTALS_ROOT = "mil.darpa.immortals.root";
	private static final String JARGS_EXTERNAL_SERVER_PORT = "mil.darpa.immortals.flitcons.test.server.external.port";
	private static final String JARGS_EXTERNAL_SERVER_HOST = "mil.darpa.immortals.flitcons.test.server.external.host";
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

	private static boolean hasImmortalsRoot() {
		String immortalsRoot = System.getProperty(JARGS_IMMORTALS_ROOT);
		return immortalsRoot != null;
	}

	public static Path getImmortalsRoot() {
		String immortalsRoot = System.getProperty(JARGS_IMMORTALS_ROOT);
		Path immortalsRootPath;
		if (immortalsRoot == null) {
			throw new RuntimeException(JARGS_IMMORTALS_ROOT + " must be set!");
		} else if (!Files.exists(immortalsRootPath = Paths.get(immortalsRoot))) {
			throw new RuntimeException("immortalsRoot '" + immortalsRootPath.toString() + "' does not exist!");
		}
		return immortalsRootPath;
	}

	static {
		System.out.println("---------------------------------INIT VARIABLES---------------------------------");
		System.out.println("testDatabaseDirectory='" + getTestDatabaseDirectory() + "'");
		if (hasImmortalsRoot()) {
			System.out.println("immortalsRoot='" + getImmortalsRoot() + "'");
		} else {
			System.out.println("immortalsRoot=NULL");
		}
		System.out.println("odbExternalServerPort='" + (getOdbExternalServerPort() == null ? "<NONE>" : getOdbExternalServerPort()));
		System.out.println("odbExternalServerHost='" + (getOdbExternalServerHost() == null ? "<NONE>" : getOdbExternalServerHost()));
		System.out.println("--------------------------------------------------------------------------------");
	}

}
