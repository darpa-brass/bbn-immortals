package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicValueeException;
import mil.darpa.immortals.flitcons.reporting.AdaptationnException;
import mil.darpa.immortals.flitcons.reporting.ResultEnum;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DslSolver implements SolverInterface {

	private final Path dslDirectory;

	public DslSolver() {
		try {
			String dslPath = SolverConfiguration.getInstance().dslPath;
			Path cwd = Paths.get(DslSolver.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			if (dslPath == null) {
				dslDirectory = cwd.getParent().getParent().getParent().resolve("dsl").resolve("resource-dsl").toAbsolutePath().normalize();
			} else {
				dslDirectory = cwd.resolve(dslPath).toAbsolutePath().normalize();
			}

			if (!Files.exists(dslDirectory)) {
				throw AdaptationnException.internal("Path '" + dslDirectory.toString() + "' does not exist!");
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
			applyHacks(inputConfigurationClone, inventoryClone);

			FileUtils.writeStringToFile(dslDirectory.resolve("inbox").resolve("swap-request.json").toFile(), Utils.difGson.toJson(inputConfigurationClone), Charset.defaultCharset());
			FileUtils.writeStringToFile(dslDirectory.resolve("inbox").resolve("swap-inventory.json").toFile(), Utils.difGson.toJson(inventoryClone), Charset.defaultCharset());
			FileUtils.forceMkdir(dslDirectory.resolve("outbox").toFile());

		} catch (IOException e) {
			throw AdaptationnException.internal(e);
		}
	}

	private void applyHacks(@Nonnull DynamicObjectContainer inputConfiguration, @Nonnull DynamicObjectContainer inventory) {
		try {
			for (DynamicObjectContainer dau : inputConfiguration.get("daus").parseDynamicObjectContainerArray()) {
				for (DynamicObjectContainer port : dau.get("Port").parseDynamicObjectContainerArray()) {
					port.remove("DataRate");
					port.remove("SampleRate");
					port.remove("DataLength");
				}
			}

			for (DynamicObjectContainer dau : inventory.get("daus").parseDynamicObjectContainerArray()) {
				for (DynamicObjectContainer port : dau.get("Port").parseDynamicObjectContainerArray()) {
					port.remove("DataRate");
					port.remove("SampleRate");
					port.remove("DataLength");
				}
			}
		} catch (DynamicValueeException e) {
			throw AdaptationnException.internal(e);
		}
	}

	@Override
	public DynamicObjectContainer solve() {
		try {
			System.out.println("########################START DSL OUTPUT HERE########################");

			ProcessBuilder pb = new ProcessBuilder()
					.inheritIO()
					.directory(dslDirectory.toFile())
					.command("stack", "exec", "resource-dsl", "--", "swap-dau", "--run");

			Process p = pb.start();

			int rval = p.waitFor();


			System.out.println("########################END DSL OUTPUT HERE########################");

			if (rval == 0) {


				Path dslOutputFile = dslDirectory.resolve("outbox").resolve("swap-response.json");

				if (!Files.exists(dslOutputFile)) {
					throw AdaptationnException.internal("DSL Output file not found!");
				}

				FileReader fr = new FileReader(dslOutputFile.toFile());
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
