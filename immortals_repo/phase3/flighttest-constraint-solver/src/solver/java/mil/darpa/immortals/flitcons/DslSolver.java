package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.EnvironmentConfiguration;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DslSolver implements SolverInterface {

	public static final String ARGS_DSL_PATH = "IMMORTALS_RESOURCE_DSL";

	private static final String SWAP_REQUEST = "dsl-swap-request.json";
	private static final String SWAP_INVENTORY = "dsl-swap-inventory.json";
	private static final String SWAP_RESPONSE = "dsl-swap-response.json";

	private final Path dslDirectory;

	private String requestPath;
	private String inventoryPath;
	private Path responsePath;

	public DslSolver() {
		try {
			String envPath;
			Path cwd = Paths.get(DslSolver.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if ((envPath = System.getenv(ARGS_DSL_PATH)) != null) {
				dslDirectory = Paths.get(envPath);

			} else {
				dslDirectory = cwd.getParent().getParent().getParent().resolve("dsl").resolve("resource-dsl").toAbsolutePath().normalize();
			}

			if (!Files.exists(dslDirectory)) {
				if (envPath == null) {
					throw AdaptationnException.internal("No path for the DSL provided and relative path '" + dslDirectory.toAbsolutePath() + "' does not exist!");
				} else {
					throw AdaptationnException.internal("Provided DSL path '" + dslDirectory.toString() + "' does not exist!");
				}
			}

		} catch (URISyntaxException e) {
			throw AdaptationnException.internal(e);
		}
	}

	@Override
	public void loadData(@Nonnull DynamicObjectContainer inputConfiguration, @Nonnull DynamicObjectContainer inventory) {
		try {
			DynamicObjectContainer inputConfigurationClone = inputConfiguration.duplicate();
			DynamicObjectContainer inventoryClone = inventory.duplicate();

			requestPath = EnvironmentConfiguration.storeFile(SWAP_REQUEST, Utils.difGson.toJson(inputConfigurationClone).getBytes());
			inventoryPath = EnvironmentConfiguration.storeFile(SWAP_INVENTORY, Utils.difGson.toJson(inventoryClone).getBytes());
			responsePath = Paths.get(EnvironmentConfiguration.storeFile(SWAP_RESPONSE, new byte[0]));
			Files.delete(responsePath);
			FileUtils.forceMkdir(dslDirectory.resolve("outbox").toFile());

		} catch (Exception e) {
			throw AdaptationnException.internal(e);
		}
	}

	@Override
	public DynamicObjectContainer solve() {
		try {
			System.out.println(Utils.padCenter("DSL COMMAND", 80, '#'));

			ProcessBuilder pb = new ProcessBuilder()
					.inheritIO()
					.directory(dslDirectory.toFile())
					.command("stack", "exec", "resource-dsl", "--", "swap-dau", "--run",
							"--inventory-file", inventoryPath, "--request-file", requestPath,
							"--response-file", responsePath.toString());

			System.out.println(String.join(" ", pb.command()));

			System.out.println(Utils.padCenter("DSL OUTPUT", 80, '#'));

			Process p = pb.start();
			int rval = p.waitFor();

			System.out.println("########################END DSL OUTPUT HERE########################");

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
