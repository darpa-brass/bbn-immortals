package mil.darpa.immortals.das.sourcecomposer;

import mil.darpa.immortals.das.configuration.EnvironmentConfiguration;
import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 10/11/16.
 */
public class ApplicationAugmenter {

    public static Path intializeApplicationInstance(EnvironmentConfiguration.CompositionTargetProfile targetProfile, String sessionIdentifier) throws IOException {
        Path targetPath = targetProfile.generateTargetApplicationPathValue(sessionIdentifier);

        Files.createDirectories(targetPath);
        FileUtils.copyDirectory(targetProfile.getSourceApplicationFilepath().toFile(), targetPath.toFile());

        return targetPath;
    }

    public static void replaceClass(EnvironmentConfiguration.CompositionTargetProfile targetProfile, String applicationPath, String originalClasspath, String newClasspath) throws IOException {

        for (String filepath : targetProfile.synthesisTargetFiles) {
            Path targetPath = Paths.get(applicationPath, filepath);
            List<String> inputLines = Files.readAllLines(targetPath);
            ArrayList<String> outputLines = new ArrayList<>(inputLines.size());

            String originalClassname = originalClasspath.substring(originalClasspath.lastIndexOf(".") + 1, originalClasspath.length());

            for (String inputLine : inputLines) {
                if (inputLine.contains(originalClasspath)) {
                    outputLines.add(inputLine.replaceAll(originalClasspath, newClasspath));
                } else if (inputLine.contains("new " + originalClassname)) {
                    outputLines.add(inputLine.replaceAll("new " + originalClassname, "new " + newClasspath));
                } else {
                    outputLines.add(inputLine);
                }
            }

            Files.write(targetPath, outputLines, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }
}
