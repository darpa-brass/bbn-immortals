package mil.darpa.immortals.flitcons.solvers.dsl;

import mil.darpa.immortals.schemaevolution.ChallengeProblemBridge;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DslSolver {
    private final Path cwd;
    private final Path dslDirectory;


    private DSLInterchangeFormat inputData;
    private File dslInputFile;

    public DslSolver() {
        try {
            cwd = Paths.get(DslSolver.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            dslDirectory = cwd.getParent().getParent().getParent().resolve("dsl").resolve("resource-dsl").toAbsolutePath().normalize();
            if (!Files.exists(dslDirectory)) {
                throw new RuntimeException("Path '" + dslDirectory.toString() + "' does not exist!");
            }


        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadData(DSLInterchangeFormat inputData) {
        this.inputData = inputData;
        this.dslInputFile = dslDirectory.resolve("inbox").resolve("swap-request.json").toFile();
        inputData.writeToDslInputFile(dslInputFile);
    }

    public DSLInterchangeFormat solve() throws Exception {
        System.out.println("########################START DSL OUTPUT HERE########################");

        ProcessBuilder pb = new ProcessBuilder()
                .inheritIO()
                .directory(dslDirectory.toFile())
                .command("stack", "exec", "resource-dsl",  "--", "swap-dau", "--run");

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

    }
}
