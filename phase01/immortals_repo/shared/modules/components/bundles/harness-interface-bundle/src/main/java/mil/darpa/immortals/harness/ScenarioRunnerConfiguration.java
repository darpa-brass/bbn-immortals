package mil.darpa.immortals.harness;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 11/1/16.
 */
public class ScenarioRunnerConfiguration {

    public ScenarioConfiguration scenarioConfiguration;
    public final LinkedList<String> scenarioIdentifiers = new LinkedList<>();
    public boolean validate = true;
    public int timeout = 60;
    public boolean setupEnvironment = true;
    public boolean setupApplications = true;
    public boolean executeScenario = true;
    public boolean keepEnvironmentRunning = false;
    public boolean wipeExistingEnvironment = true;
    public boolean displayEmulatorGui = false;


    public static ScenarioRunnerConfiguration loadDefaultConfiguration(ScenarioConfiguration scenarioConfiguration,
                                                                       int timeout,
                                                                       List<String> scenarioIdentifiers) {
        ClassLoader cl = ScenarioRunnerConfiguration.class.getClassLoader();
        InputStream is = cl.getResourceAsStream("/DefaultScenarioRunnerConfiguration.json");
        Gson gson = new Gson();
        JsonReader jr = new JsonReader(new InputStreamReader(is));
        ScenarioRunnerConfiguration src = gson.fromJson(jr, ScenarioRunnerConfiguration.class);
        src.scenarioConfiguration = scenarioConfiguration;
        src.timeout = timeout;
        src.scenarioIdentifiers.addAll(scenarioIdentifiers);

        return src;
    }
}
