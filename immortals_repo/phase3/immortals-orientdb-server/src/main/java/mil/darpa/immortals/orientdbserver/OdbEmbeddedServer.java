package mil.darpa.immortals.orientdbserver;

import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.core.Orient;
import com.orientechnologies.orient.core.command.OCommandOutputListener;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OConcurrentResultSet;
import com.orientechnologies.orient.server.OServer;
import com.orientechnologies.orient.server.OServerMain;
import com.orientechnologies.orient.server.config.OServerConfiguration;
import com.orientechnologies.orient.server.config.OServerConfigurationManager;
import com.orientechnologies.orient.server.config.OServerEntryConfiguration;
import com.orientechnologies.orient.server.network.protocol.binary.ONetworkProtocolBinary;
import com.orientechnologies.orient.server.network.protocol.http.ONetworkProtocolHttpAbstract;
import mil.darpa.immortals.EnvironmentConfiguration;
import mil.darpa.immortals.schemaevolution.TerminalStatus;
import org.apache.tools.ant.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class OdbEmbeddedServer {

	public enum OdbDeploymentMode {
		BackupsOnly,
		XmlOnly,
		BackupsWithUpdatedXmlIfAvailable,
		BackupsWithUpdatedXml
	}

	private static final String ENV_ACS = "IMMORTALS_ADAPTIVE_CONSTRAINT_SATISFACTION_ROOT";

	private static final Logger logger = LoggerFactory.getLogger(OdbEmbeddedServer.class);

	private static final Object lock = new Object();

	protected final TestScenario[] scenarios;
	private final String host;
	private int port;
	private OServer server;

	public OdbEmbeddedServer(@Nonnull TestScenario... scenarios) {
		this.scenarios = scenarios;
		this.host = "127.0.0.1";
	}

	public String getOdbPath(@Nonnull TestScenario scenario) {
		return "remote" + ":" + host + ":" + port + "/" + scenario.getDbName();
	}

	private boolean restoreDatabase(@Nonnull TestScenario scenario) {
		synchronized (lock) {
			InputStream scenarioStream = scenario.getBackupInputStream();

			if (scenarioStream == null) {
				return false;
			}

			logger.info("Restoring database from backup for '" + scenario.getShortName() + "'...");
			ODatabaseDocumentTx db = null;
			try {
				OServerAdmin admin = new OServerAdmin(host + ":" + port)
						.connect("root", "g21534bn890cf57b23n405f987vnb23dh789");

				if (admin.existsDatabase(scenario.getDbName(), "plocal")) {
					admin.dropDatabase(scenario.getDbName(), "plocal");
				}
				admin.createDatabase(scenario.getDbName(), "graph", "plocal");
				admin.close();

				db = new ODatabaseDocumentTx("plocal:/" + scenario.getDbName());
				OCommandOutputListener listener = logger::debug;
				db.open("admin", "admin");
				db.restore(scenarioStream, null, null, listener);
				db.commit();

				logger.debug("Finished restoring database from backup for '" + scenario.getShortName() + "'...");
				return true;

			} catch (IOException e) {
				throw new RuntimeException(e);

			} finally {
				if (db != null) {
					db.close();
				}
			}

		}
	}

	private void backupDatabase(@Nonnull TestScenario scenario) throws IOException {
		logger.info("Creating database backup for " + scenario.getShortName() + "'...");
		synchronized (lock) {

			ODatabaseDocumentTx db = null;
			try {
				Path dbBackupDir = EnvironmentConfiguration.getArtifactDirectory().resolve("PRODUCED_TEST_DATABASES");
				if (!Files.exists(dbBackupDir)) {
					Files.createDirectory(dbBackupDir);
				}
				File dbBackupTarget = dbBackupDir.resolve(scenario.getShortName() + "-backup.zip").toFile();
				logger.debug("Backing up constructed database to '" + dbBackupTarget.getName() + "'...");
				db = new ODatabaseDocumentTx("plocal:/" + scenario.getDbName());
				if (db.isClosed()) {
					db.open("admin", "admin");
				}
				OCommandOutputListener listener = logger::debug;

				FileOutputStream os = new FileOutputStream(dbBackupTarget);
				db.backup(os, null, null, listener, 9, 2048);
//				db.commit();
				logger.debug("Finished creating database backup for " + scenario.getShortName() + "'...");
			} finally {
				if (db != null) {
					db.close();
				}
			}
		}
	}

	public void waitForEvaluatorTurn(@Nonnull TestScenario scenario) {
		TerminalStatus state = null;
		while ((state == null || (state.isEvaluationServerBlocked() && !state.isTerminal())) && isRunning()) {
			synchronized (lock) {
				if (isRunning()) {
					ODatabaseDocumentTx db = null;
					try {
						db = new ODatabaseDocumentTx("plocal:/" + scenario.getDbName());
						db.open("admin", "admin");
						OConcurrentResultSet rval = db.command(new OCommandSQL("SELECT currentState FROM BBNEvaluationData")).execute();
						ODocument result = (ODocument) rval.get(0);
						Object val = result.field("currentState");
						if (val != null) {
							state = TerminalStatus.valueOf(val.toString());
						}
					} finally {
						if (db != null && !db.isClosed()) {
							db.close();
						}
					}
				}
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void setState(@Nonnull TestScenario scenario, @Nonnull TerminalStatus state, boolean clearOtherStateValues) {
		synchronized (lock) {
			ODatabaseDocumentTx db = new ODatabaseDocumentTx("plocal:/" + scenario.getDbName());
			db.open("admin", "admin");
			db.command(new OCommandSQL("UPDATE BBNEvaluationData SET currentState = '" + state.name() + "'")).execute();
			if (clearOtherStateValues) {
				db.command(new OCommandSQL("UPDATE BBNEvaluationData REMOVE inputJsonData")).execute();
				db.command(new OCommandSQL("UPDATE BBNEvaluationData REMOVE outputJsonData")).execute();
				db.command(new OCommandSQL("UPDATE BBNEvaluationData REMOVE currentStateInfg")).execute();
			}

			logger.info("Setting currentState to 'ReadyForAdaptation...'");
			db.commit();
			logger.debug("Finished setting currentState to 'ReadForAdaptation.");
			db.close();
		}
	}

	public void clearState(@Nonnull TestScenario scenario) {
		synchronized (lock) {
			ODatabaseDocumentTx db = new ODatabaseDocumentTx("plocal:/" + scenario.getDbName());
			db.open("admin", "admin");
			db.command(new OCommandSQL("UPDATE BBNEvaluationData REMOVE currentState")).execute();
			logger.info("Clearing currentState...");
			db.commit();
			logger.debug("Finished clearing currentState.");
			db.commit();
			db.close();
		}
	}

	public synchronized void init(OdbDeploymentMode deploymentMode) {
		try {

			if (deploymentMode == OdbDeploymentMode.BackupsWithUpdatedXml && !EnvironmentConfiguration.CHALLENGE_PROBLEMS_ROOT.isPresent()) {
				throw new RuntimeException("Cannot use the deployment mode '" + deploymentMode.name() +
						"' without a challenge-problems-root set! " + EnvironmentConfiguration.CHALLENGE_PROBLEMS_ROOT.getDisplayableUsage());
			}

//			Orient.instance().startup();
			logger.info("Starting Embedded OrientDB Server...");
			server = OServerMain.create();

			OServerConfigurationManager configManager = new OServerConfigurationManager(OdbEmbeddedServer.class.getClassLoader().getResourceAsStream("odb_test_cfg.xml"));
			OServerConfiguration config = configManager.getConfiguration();
			ArrayList<OServerEntryConfiguration> properties = new ArrayList<>(Arrays.asList(config.properties));
			properties.add(new OServerEntryConfiguration("server.database.path", Paths.get("").resolve("databases").toAbsolutePath().toString()));
			config.properties = properties.toArray(new OServerEntryConfiguration[0]);

			server.startup(config);
			server.activate();
			int port = server.getListenerByProtocol(ONetworkProtocolBinary.class).getInboundAddr().getPort();
			int webPort = server.getListenerByProtocol(ONetworkProtocolHttpAbstract.class).getInboundAddr().getPort();

			StringBuilder displayMessageBuilder = new StringBuilder(
					"Embedded OrientDB is now ready for use. Details:\n" +
							"\tHost: " + host + "\n" +
							"\tPort: " + port + "\n" +
							"\tWebsite: http://" + host + ":" + webPort + "/studio/index.html\n" +
							"\tDatabases:\n");


			for (TestScenario scenario : scenarios) {
				init_embeddedRemoteAgnostic(host, port, scenario, deploymentMode);
				displayMessageBuilder.append("\t\tremote:").append(host).append(":").append(port).append("/")
						.append(scenario.getDbName()).append("\n");
			}

			logger.info(displayMessageBuilder.toString());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void initFromFiles(@Nonnull String host, int port, @Nonnull TestScenario scenario) {
		try {
			if (scenario.getScenarioType().isScenario5) {

				if (!scenario.hasXmlInventoryInput() && !scenario.hasXmlMdlrootInput()) {
					throw new RuntimeException("No MDLRoot or DAUInventory file exists for scenario '" + scenario.getShortName() + "'!");
				}

				logger.info("Constructing  OrientDB data for scenario '" + scenario.getShortName() + "' using Python scripts...");
				InputStream scriptContentsStream = OdbEmbeddedServer.class.getClassLoader().getResourceAsStream("scripts/immortals_phase3_init_scenario5.py");
				if (scriptContentsStream == null) {
					throw new RuntimeException("Could not load resource 'scripts/immortals_phase3_init_scenario5.py'!");
				}
				InputStreamReader reader = new InputStreamReader(scriptContentsStream);
				String scriptContents = FileUtils.readFully(reader);
				scriptContents = scriptContents.replaceAll("\\\\", "\\\\\\\\");
				scriptContents = scriptContents.replaceAll("\n", "\\\\n");
				scriptContents = scriptContents.replaceAll("\"", "\\\\\"");

				String[] command = new String[]{"python3", "-c", "exec(\"" + scriptContents + "\")",
						"--host", host,
						"--port", Integer.toString(port),
						"--username", "admin",
						"--password", "admin",
						"--root-password", "g21534bn890cf57b23n405f987vnb23dh789",
						"--dau-inventory-xml-file", scenario.getXmlInventoryPath().toString(),
						"--input-configuration-xml-file", scenario.getXmlMdlrootInputPath().toString(),
						"--database-name", scenario.getDbName(),
						"--no-set-ready"
				};

				ProcessBuilder pb = new ProcessBuilder()
						.command(command);

				if (logger.isDebugEnabled()) {
					pb.inheritIO();
				} else {
					File logFile = EnvironmentConfiguration.getArtifactDirectory().resolve("brass_api_log-" + scenario.getShortName() + ".log").toAbsolutePath().toFile();
					if (logFile.exists()) {
						throw new RuntimeException("The logging file '" + logFile + "' already exists!");
					}
					pb.redirectOutput(logFile);
				}

				String acsDir;
				if ((acsDir = System.getenv(ENV_ACS)) != null && new File(acsDir).exists()) {
					pb.environment().put(ENV_ACS, System.getenv(ENV_ACS));
				}

				Process p = pb.start();
				try {
					p.waitFor();
				} catch (InterruptedException e) {

					logger.error("CMD: [" + String.join(" ", command) + "]");
					logger.error("CMD: [" + String.join(" ", command) + "]");
					logger.error("COMMAND INTERRUPTED!");
					throw e;

				}

				if (p.exitValue() == 0) {
					logger.info("Finished populating the OrientDB instance.");
					backupDatabase(scenario);
				} else {
					logger.info("CMD: [" + String.join(" ", command) + "]");
					throw new RuntimeException("Invalid Server Setup exit code '" + p.exitValue() + "'!");
				}

			} else if (scenario.getScenarioType().isScenario6) {

				synchronized (lock) {
					ODatabaseDocumentTx db = null;
					try {
						OServerAdmin admin = new OServerAdmin(host + ":" + port)
								.connect("root", "g21534bn890cf57b23n405f987vnb23dh789");

						if (admin.existsDatabase(scenario.getDbName(), "plocal")) {
							admin.dropDatabase(scenario.getDbName(), "plocal");
						}

						logger.info("Constructing " + scenario.getScenarioType() + " OrientDB data in plocal storage...");
						admin.createDatabase(scenario.getDbName(), "graph", "plocal");
						admin.close();

						db = new ODatabaseDocumentTx("plocal:/" + scenario.getDbName());
						db.open("admin", "admin");

						db.command(new OCommandSQL("CREATE CLASS BBNEvaluationData EXTENDS V")).execute();
						db.command(new OCommandSQL("CREATE PROPERTY BBNEvaluationData.inputJsonData STRING")).execute();
						db.command(new OCommandSQL("CREATE PROPERTY BBNEvaluationData.outputJsonData STRING")).execute();
						db.command(new OCommandSQL("CREATE PROPERTY BBNEvaluationData.currentState STRING")).execute();
						db.command(new OCommandSQL("CREATE PROPERTY BBNEvaluationData.currentStateInfo STRING")).execute();

						String data = scenario.getInputJsonDataString();

						ODocument doc = new ODocument("BBNEvaluationData");
						doc.field("inputJsonData", data);
						doc.save();
						db.commit();

						logger.info("OrientDB scenario data restoration finished.");
					} finally {
						if (db != null && !db.isClosed()) {
							db.close();
						}
					}
					backupDatabase(scenario);
				}
			} else {
				throw new RuntimeException("Unexpected Scenario Type '" + scenario.getScenarioType() + "'!");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void init_embeddedRemoteAgnostic(@Nonnull String host, int port, @Nonnull TestScenario scenario, OdbDeploymentMode deploymentMode) {
		this.port = port;

		switch (deploymentMode) {
			case BackupsOnly:
				if (!restoreDatabase(scenario)) {
					throw new RuntimeException("Failed to restore database '" + scenario.getDbName() + "' from backup!");
				}
				break;

			case XmlOnly:
				initFromFiles(host, port, scenario);
				break;

			case BackupsWithUpdatedXmlIfAvailable:
				if (((scenario.hasXmlInventoryInput() || scenario.hasXmlMdlrootInput()) && scenario.backupIsOutdated()) ||
						!scenario.hasBackup()) {
					initFromFiles(host, port, scenario);
				} else {
					if (!restoreDatabase(scenario)) {
						throw new RuntimeException("Could not restore the test scenario '" + scenario.getShortName() + "'!");
					}
				}
				break;

			case BackupsWithUpdatedXml:
				if (!(scenario.hasXmlInventoryInput() || scenario.hasXmlMdlrootInput() || scenario.hasUpdatedXsdInputPath())) {
					throw new RuntimeException("No input XML files could be found!");
				}

				if (scenario.backupIsOutdated() || !scenario.hasBackup()) {
					initFromFiles(host, port, scenario);
				} else {
					if (!restoreDatabase(scenario)) {
						throw new RuntimeException("Could not restore the test scenario '" + scenario.getShortName() + "'!");
					}
				}
				break;
		}
		System.setProperty(EnvironmentConfiguration.ODB_TARGET.javaArg, "remote:" + host + ":" + port + "/" + scenario.getDbName());
	}

	public boolean isRunning() {
		if (server == null) {
			return false;
		}
		return server.isActive();
	}

	public synchronized void shutdown() {
		if (server != null) {
			logger.info("Shutting Down Embedded OrientDB Server.");
			server.shutdown();
			Orient.instance().shutdown();
		}
	}

	public synchronized void waitForShutdown() {
		server.waitForShutdown();
	}
}
