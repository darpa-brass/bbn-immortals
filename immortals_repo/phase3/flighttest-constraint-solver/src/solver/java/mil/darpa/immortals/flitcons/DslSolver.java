package mil.darpa.immortals.flitcons;

import com.google.gson.JsonObject;
import mil.darpa.immortals.EnvironmentConfiguration;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainerFactory;
import mil.darpa.immortals.flitcons.mdl.MdlHacks;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DslSolver implements SolverInterface<DslSolver> {

	private static Process dslProcess = null;

	private static final String SWAP_REQUEST = "dsl-swap-request.json";
	private static final String SWAP_INVENTORY = "dsl-swap-inventory.json";
	private static final String SWAP_RESPONSE = "dsl-swap-response.json";
	private static final String SWAP_RULES = "dsl-swap-rules.json";
	private static final String SWAP_METRICS = "dsl-swap-metrics.json";
	private static final Logger logger = LoggerFactory.getLogger(DslSolver.class);

	private final Path dslDirectory;

	private Path requestPath;
	private Path inventoryPath;
	private Path rulesPath;
	private Path responsePath;
	private Path metricsPath;

	public DslSolver() {
		dslDirectory = EnvironmentConfiguration.getDslRoot();
	}

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			Process p = dslProcess;
			if (p != null) {
				p.destroy();
			}
		}));
	}

	@Override
	public DslSolver loadData(@Nonnull AbstractDataSource dataSource) {
		try {
			DynamicObjectContainer inputConfiguration = DynamicObjectContainerFactory.create(dataSource.getInterconnectedTransformedFaultyConfiguration(false));
			DynamicObjectContainer dauInventory = DynamicObjectContainerFactory.create(dataSource.getTransformedDauInventory(false));

			requestPath = Paths.get(EnvironmentConfiguration.storeFile(SWAP_REQUEST, Utils.difGson.toJson(inputConfiguration).getBytes()));
			inventoryPath = Paths.get(EnvironmentConfiguration.storeFile(SWAP_INVENTORY, Utils.difGson.toJson(dauInventory).getBytes()));
			responsePath = Paths.get(EnvironmentConfiguration.storeFile(SWAP_RESPONSE, new byte[0]));
			metricsPath = Paths.get(EnvironmentConfiguration.storeFile(SWAP_METRICS, new byte[0]));
			rulesPath = Paths.get(EnvironmentConfiguration.storeFile(SWAP_RULES, Utils.getGson().toJson(Configuration.getInstance().adaptation.resolutionOptions).getBytes()));
			Files.delete(responsePath);
			FileUtils.forceMkdir(dslDirectory.resolve("outbox").toFile());

		} catch (Exception e) {
			throw AdaptationnException.internal(e);
		}
		return this;
	}

	@Override
	public DynamicObjectContainer solveFromJsonFiles(@Nonnull Path inputJsonFile, @Nonnull Path inventoryJsonFile) {
		try {
			requestPath = inputJsonFile;
			inventoryPath = inventoryJsonFile;
			responsePath = Paths.get(EnvironmentConfiguration.storeFile(SWAP_RESPONSE, new byte[0]));
			metricsPath = Paths.get(EnvironmentConfiguration.storeFile(SWAP_METRICS, new byte[0]));
			rulesPath = Paths.get(EnvironmentConfiguration.storeFile(SWAP_RULES, Utils.getGson().toJson(Configuration.getInstance().adaptation.resolutionOptions).getBytes()));
			Files.delete(responsePath);
			Files.delete(metricsPath);
			FileUtils.forceMkdir(dslDirectory.resolve("outbox").toFile());
		} catch (Exception e) {
			throw AdaptationnException.internal(e);
		}

		return this.solve();
	}

	@Override
	public DynamicObjectContainer solve() {
		try {
			if (dslProcess != null) {
				throw AdaptationnException.internal("Cannot run Two DSLs at the same time!");
			}
			logger.info(Utils.padCenter("DSL COMMAND", 80, '#'));

			String[] cmd = {
					"stack", "exec", "resource-dsl", "--", "swap-dau", "--run",
					"--max-daus", Integer.toString(EnvironmentConfiguration.getMaxDauSelectionCount()),
					"--rules-file", rulesPath.toString(),
					"--inventory-file", inventoryPath.toString(), "--request-file", requestPath.toString(),
					"--response-file", responsePath.toString()
			};

			final ProcessBuilder pb = new ProcessBuilder(cmd)
					.inheritIO()
					.directory(dslDirectory.toFile());
			logger.info(String.join(" ", pb.command()));

			logger.info(Utils.padCenter("DSL OUTPUT", 80, '#'));

			dslProcess = pb.start();

			int rval = dslProcess.waitFor();

			dslProcess = null;

			logger.info("########################END DSL OUTPUT HERE########################");

			if (rval == 0) {
				Path metricsFile = dslDirectory.resolve("outbox/swap-metrics.json");
				if (Files.exists(metricsPath)) {
					FileUtils.copyFile(metricsFile.toFile(), metricsPath.toFile());
				}


//				Path dslOutputFile = dslDirectory.resolve("outbox").resolve("swap-response.json");

				if (!Files.exists(responsePath)) {
					throw AdaptationnException.internal("DSL Output file not found!");
				}

				FileReader fr = new FileReader(responsePath.toFile());
				DynamicObjectContainer result = Utils.difGson.fromJson(fr, DynamicObjectContainer.class);

				MdlHacks.cleanseDslOutput(result);

				return result;

			} else if (rval == 1) {
				throw AdaptationnException.input("DSL indicated an invalid input configuration!");

			} else if (rval == 3) {
				throw new AdaptationnException(ResultEnum.AdaptationUnsuccessful, "No Solution Found!");

			} else {
				throw AdaptationnException.internal("DSL exited with an unexpected failure status!");
			}
		} catch (IOException | InterruptedException e) {
			throw AdaptationnException.internal(e);
		}
	}

	@Nullable
	@Override
	public JsonObject getMetrics() {
		try {
			if (Files.exists(metricsPath)) {
				return Utils.getGson().fromJson(new FileReader(metricsPath.toFile()), JsonObject.class);
			}
			return null;
		} catch (FileNotFoundException e) {
			throw AdaptationnException.internal(e);
		}
	}
}
