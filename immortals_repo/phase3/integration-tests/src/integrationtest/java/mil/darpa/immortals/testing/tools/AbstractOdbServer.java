package mil.darpa.immortals.testing.tools;

import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.core.command.OCommandOutputListener;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OConcurrentResultSet;
import mil.darpa.immortals.schemaevolution.ProvidedData;
import mil.darpa.immortals.schemaevolution.TerminalStatus;
import org.testng.Assert;

import javax.annotation.Nonnull;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static mil.darpa.immortals.schemaevolution.ProvidedData.JARGS_ARTIFACT_DIRECTORY;
import static mil.darpa.immortals.schemaevolution.ProvidedData.JARGS_EVAL_ODB;


public abstract class AbstractOdbServer {

	protected final TestScenario scenario;
	private String storageMode;
	private String host;
	private int port;

	public AbstractOdbServer(@Nonnull TestScenario scenario) {
		this.scenario = scenario;
	}

	public String getStorageMode() {
		return storageMode;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getOdbPath() {
			return "remote" + ":" + host + ":" + port + "/" + scenario.getDbName();
	}
	public abstract void init();

	protected void init(@Nonnull String storageMode, @Nonnull String host, int port) {
		this.storageMode = storageMode;
		this.host = host;
		this.port = port;

		try {
			if (scenario.getBackupFile().exists() && !storageMode.equals("remote")) {
				restoreDatabase();
			} else {

				Path tools = null;
				Path cwd = Paths.get("").toAbsolutePath();
				while (!cwd.toString().equals("/")) {
					tools = cwd.resolve("shared/tools.sh");
					if (Files.exists(cwd.resolve("shared/tools.sh"))) {
						break;
					}
					tools = null;
					cwd = cwd.getParent();
				}

				if (tools == null) {
					Assert.fail("Could not find tools directory to populate server!");
				}

				// TODO: Port Scenario6 setup logic from python to this since it should be trivial

				String[] command = {
						tools.toString(),
						"odbhelper",
						"start",
						"--host", host,
						"--port", Integer.toString(port),
						"--use-default-root-password",
						"-r", scenario.getShortName()
				};

				ProcessBuilder pb = new ProcessBuilder()
						.command(command)
						.redirectOutput(ProcessBuilder.Redirect.INHERIT)
						.redirectError(ProcessBuilder.Redirect.INHERIT);

				System.out.println("CMD: [" + String.join(" ", command) + "]");

				Process p = pb.start();
				p.waitFor();

				if (p.exitValue() == 0) {
					System.out.println("Finished populating the OrientDB instance.");
					if (!storageMode.equals("remote")) {
						backupDatabase();
					}
				} else {
					Assert.fail("Invalid Server Setup exit code '" + p.exitValue() + "'!");
				}
			}

			if (storageMode.equals("remote")) {
				System.setProperty(JARGS_EVAL_ODB, storageMode + ":" + host + ":" + port + "/" + scenario.getDbName());
			} else {
				System.setProperty(JARGS_EVAL_ODB, storageMode + ":" + scenario.getDbName());
			}

			Path artifactDir = ProvidedData.getEvaluationArtifactDirectory().toAbsolutePath();
			System.setProperty(JARGS_ARTIFACT_DIRECTORY, artifactDir.toString());
			if (!Files.exists(artifactDir)) {
				Files.createDirectory(artifactDir);
			}

		} catch (Exception e) {
			Assert.fail("Unexpected exception starting server!", e);
			throw new RuntimeException(e);

		}
	}


	protected synchronized void restoreDatabase() throws IOException {

		ODatabaseDocumentTx db = null;
		try {
			OServerAdmin admin = new OServerAdmin(getHost() + ":" + getPort())
					.connect("root", "g21534bn890cf57b23n405f987vnb23dh789");

			if (admin.existsDatabase(scenario.getDbName(), getStorageMode())) {
				admin.dropDatabase(scenario.getDbName(), getStorageMode());
			}
			System.out.println("Restoring OrientDB scenario data...");
			System.out.println("StorageMode: " + getStorageMode());
			admin.createDatabase(scenario.getDbName(), "graph", getStorageMode());
//			admin.createDatabase(scenario.getDbName(), "graph", getStorageMode(), scenario.getBackupFile().toString());
			admin.close();

			System.out.println("STORAGE MODE: " + getStorageMode());
			db = new ODatabaseDocumentTx(getStorageMode() + ":/" + scenario.getDbName());
			db.open("admin", "admin");
//			OCommandOutputListener listener = System.out::println;
			FileInputStream fis = new FileInputStream(scenario.getBackupFile());
			db.restore(fis, null, null, null);
			db.commit();

			System.out.println("OrientDB scenario data restoration finished.");
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	protected synchronized void backupDatabase() throws IOException {
		ODatabaseDocumentTx db = null;
		try {
			db = new ODatabaseDocumentTx(getStorageMode() + ":/" + scenario.getDbName());
			db.open("admin", "admin");
			OCommandOutputListener listener = System.out::println;

			FileOutputStream os = new FileOutputStream(scenario.getBackupFile());
			db.backup(os, null, null, listener, 9, 2048);
			db.commit();
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	protected  synchronized void runAndWait() {
		Thread evaluatorThread;
		Thread sutThread;

	}

	public synchronized void pollFinished() {
		String state = null;

		while (state == null || !state.equals(TerminalStatus.ReadyForAdaptation)) {
			ODatabaseDocumentTx db = new ODatabaseDocumentTx(getStorageMode() + ":/" + scenario.getDbName());
			db.open("admin", "admin");
			OConcurrentResultSet rval = db.command(new OCommandSQL("SELECT currentState FROM BBNEvaluationData")).execute();
			ODocument result = (ODocument) rval.get(0);
			state = result.field("currentState");
			db.close();

			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public synchronized void setReady() {
		ODatabaseDocumentTx db = new ODatabaseDocumentTx(getStorageMode() + ":/" + scenario.getDbName());
		db.open("admin", "admin");
		db.command(new OCommandSQL("UPDATE BBNEvaluationData REMOVE inputJsonData")).execute();
		db.command(new OCommandSQL("UPDATE BBNEvaluationData REMOVE outputJsonData")).execute();
		db.command(new OCommandSQL("UPDATE BBNEvaluationData REMOVE currentStateInfg")).execute();
		db.command(new OCommandSQL("UPDATE BBNEvaluationData SET currentState = 'ReadyForAdaptation'")).execute();
		db.commit();
		db.close();
	}


	public synchronized void clearState() {
		ODatabaseDocumentTx db = new ODatabaseDocumentTx(getStorageMode() + ":/" + scenario.getDbName());
		db.open("admin", "admin");
		db.command(new OCommandSQL("UPDATE BBNEvaluationData REMOVE currentState")).execute();
		db.commit();
		db.close();
	}

	public abstract void shutdown();
}
