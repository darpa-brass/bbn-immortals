package mil.darpa.immortals.flitcons;

import mil.darpa.immortals.flitcons.datatypes.dynamic.DynamicObjectContainer;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DslSolver implements SolverInterface {


	public static File dslInputFile = new File("dsl-input-configuration.json");
	public static File dslDauInventoryFile = new File("dsl-dau-inventory.json");

	private final boolean mockSolve;
	private final Path dslDirectory;

	public DslSolver() {
		if (System.getProperty("mil.darpa.immortals.mock.dsl") != null &&
				System.getProperty("mil.darpa.immortals.mock.dsl").toLowerCase().equals("true")) {
			mockSolve = true;
			dslDirectory = null;
			return;
		}

		mockSolve = false;

		try {
			Path cwd = Paths.get(DslSolver.class.getProtectionDomain().getCodeSource().getLocation().toURI());
			dslDirectory = cwd.getParent().getParent().getParent().resolve("dsl").resolve("resource-dsl").toAbsolutePath().normalize();
			if (!Files.exists(dslDirectory)) {
				throw new RuntimeException("Path '" + dslDirectory.toString() + "' does not exist!");
			}


		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void loadData(@Nonnull DynamicObjectContainer inputConfiguration, @Nonnull DynamicObjectContainer inventory) {
		try {
			String inputJson = Utils.difGson.toJson(inputConfiguration);
			String inventoryJson = Utils.difGson.toJson(inventory);

			FileUtils.writeStringToFile(dslInputFile, inputJson, Charset.defaultCharset());
			FileUtils.writeStringToFile(dslDirectory.resolve("inbox").resolve("swap-request.json").toFile(), inputJson, Charset.defaultCharset());

			FileUtils.writeStringToFile(dslDauInventoryFile, inventoryJson, Charset.defaultCharset());
			FileUtils.writeStringToFile(dslDirectory.resolve("inbox").resolve("swap-inventory.json").toFile(), inventoryJson, Charset.defaultCharset());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public DynamicObjectContainer solve() {
		if (mockSolve) {
			return mockSolve();
		}

		try {
			System.out.println("########################START DSL OUTPUT HERE########################");

			ProcessBuilder pb = new ProcessBuilder()
					.inheritIO()
					.directory(dslDirectory.toFile())
					.command("stack", "exec", "resource-dsl", "--", "swap-dau", "--run");

			Process p = pb.start();

			int rval = p.waitFor();


			System.out.println("########################END DSL OUTPUT HERE########################");

			if (rval != 0) {
				throw new RuntimeException("DSL exited with non-zero status!");
			}

			Path dslOutputFile = dslDirectory.resolve("outbox").resolve("swap-response.json");

			if (!Files.exists(dslOutputFile)) {
				throw new RuntimeException("DSL Output file not found!");
			}

			return null;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private DynamicObjectContainer mockSolve() {
		System.out.println("############################NO DSL IN USE############################");
		InputStream is = DslSolver.class.getClassLoader().getResourceAsStream("dummy_data/mock-dsl-output.json");
		if (is == null) {
			throw new RuntimeException("Could not load dummy data for mock solver!");
		}
		InputStreamReader isr = new InputStreamReader(is);
		return Utils.difGson.fromJson(isr, DynamicObjectContainer.class);
	}
}
