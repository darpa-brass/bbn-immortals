package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.EnvironmentConfiguration;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainerFactory;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DslSolver implements SolverInterface<DslSolver> {

	private static final String SWAP_REQUEST = "dsl-swap-request.json";
	private static final String SWAP_INVENTORY = "dsl-swap-inventory.json";
	private static final String SWAP_RESPONSE = "dsl-swap-response.json";
	private static final Logger logger = LoggerFactory.getLogger(DslSolver.class);

	private final Path dslDirectory;

	private String requestPath;
	private String inventoryPath;
	private Path responsePath;

	public DslSolver() {
		dslDirectory = EnvironmentConfiguration.getDslRoot();
	}

	@Override
	public DslSolver loadData(@Nonnull AbstractDataSource dataSource) {
		try {
			DynamicObjectContainer inputConfiguration = DynamicObjectContainerFactory.create(dataSource.getInterconnectedTransformedFaultyConfiguration(false));
			DynamicObjectContainer dauInventory = DynamicObjectContainerFactory.create(dataSource.getTransformedDauInventory(false));

			requestPath = EnvironmentConfiguration.storeFile(SWAP_REQUEST, Utils.difGson.toJson(inputConfiguration).getBytes());
			inventoryPath = EnvironmentConfiguration.storeFile(SWAP_INVENTORY, Utils.difGson.toJson(dauInventory).getBytes());
			responsePath = Paths.get(EnvironmentConfiguration.storeFile(SWAP_RESPONSE, new byte[0]));
			Files.delete(responsePath);
			FileUtils.forceMkdir(dslDirectory.resolve("outbox").toFile());

		} catch (Exception e) {
			throw AdaptationnException.internal(e);
		}
		return this;
	}

	@Override
	public DynamicObjectContainer solve() {
		try {
			logger.info(Utils.padCenter("DSL COMMAND", 80, '#'));

			ProcessBuilder pb = new ProcessBuilder()
					.inheritIO()
					.directory(dslDirectory.toFile())
					.command("stack", "exec", "resource-dsl", "--", "swap-dau", "--run",
							"--inventory-file", inventoryPath, "--request-file", requestPath,
							"--response-file", responsePath.toString());

			logger.info(String.join(" ", pb.command()));

			logger.info(Utils.padCenter("DSL OUTPUT", 80, '#'));

			Process p = pb.start();
			int rval = p.waitFor();

			logger.info("########################END DSL OUTPUT HERE########################");

			if (rval == 0) {


//				Path dslOutputFile = dslDirectory.resolve("outbox").resolve("swap-response.json");

				if (!Files.exists(responsePath)) {
					throw AdaptationnException.internal("DSL Output file not found!");
				}

				FileReader fr = new FileReader(responsePath.toFile());
				return Utils.difGson.fromJson(fr, DynamicObjectContainer.class);

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
}
