package mil.darpa.immortals.das.buildbridge;

import mil.darpa.immortals.das.configuration.EnvironmentConfiguration;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

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


    private static BuildBridge mInstance;

    private BuildBridge(EnvironmentConfiguration environmentConfiguration) {
        configuration = environmentConfiguration;
    }

    public synchronized void buildSynthesisRepository(String sessionIdentifier) {
        ProjectConnection projectConnection = GradleConnector.newConnector().forProjectDirectory(configuration.getSynthesisModulesPath(sessionIdentifier).toFile()).connect();

        projectConnection.newBuild().forTasks("publish").setStandardOutput(System.out).run();
        projectConnection.close();
    }

    public synchronized void buildApplication(Path applicationPath) {
        ProjectConnection applicationConnection = GradleConnector.newConnector().forProjectDirectory(applicationPath.toAbsolutePath().toFile()).connect();
        applicationConnection.newBuild().forTasks("build").setStandardOutput(System.out).run();
        applicationConnection.close();
    }

    // This appears to be kicking off some base ATAKLite stuff, which gets confused about where common.gradle is... Need to investigate before enabling...
//    public synchronized void wipeSynthesized() {
//        ProjectConnection applicationConnection = GradleConnector.newConnector().forProjectDirectory(configuration.getImmortalsRootPath().toAbsolutePath().toFile()).connect();
//        applicationConnection.newBuild().forTasks("wipeSynthesized").setStandardOutput(System.out).run();
//        applicationConnection.close();
//    }
}

