package mil.darpa.immortals.das.buildbridge;

import mil.darpa.immortals.das.configuration.EnvironmentConfiguration;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

import java.io.File;
import java.nio.file.Path;

/**
 * Created by awellman@bbn.com on 10/10/16.
 */
public class BuildBridge {

    public static BuildBridge getInstance() {
        if (mInstance == null) {
            mInstance = new BuildBridge(EnvironmentConfiguration.getInstance());
        }
        return mInstance;
    }

    private EnvironmentConfiguration configuration;
    private final File gradleHome;


    private static BuildBridge mInstance;

    private BuildBridge(EnvironmentConfiguration environmentConfiguration) {
        configuration = environmentConfiguration;
        String gradleHomeStr = System.getenv("GRADLE_HOME");
        if (gradleHomeStr == null) {
            gradleHome = null;
        } else {
            File gh = new File(gradleHomeStr);
            if (gh.exists()) {
                gradleHome = gh;
            } else {
                gradleHome = null;
            }
        }
    }

    public synchronized void buildSynthesisRepository(String sessionIdentifier) {
        GradleConnector gradleConnector = GradleConnector.newConnector().forProjectDirectory(configuration.getSynthesisModulesPath(sessionIdentifier).toFile());

        if (gradleHome != null) {
            gradleConnector.useInstallation(gradleHome);
        }

        ProjectConnection projectConnection = gradleConnector.connect();

        projectConnection.newBuild().forTasks("publish").withArguments("--offline").setStandardOutput(System.out).run();
        projectConnection.close();
    }

    public synchronized void buildApplication(Path applicationPath) {
        GradleConnector gradleConnector = GradleConnector.newConnector().forProjectDirectory(applicationPath.toAbsolutePath().toFile());

        if (gradleHome != null) {
            gradleConnector.useInstallation(gradleHome);
        }

        ProjectConnection applicationConnection = gradleConnector.connect();

        applicationConnection.newBuild().forTasks("build").withArguments("--offline").setStandardOutput(System.out).run();
        applicationConnection.close();
    }
}

