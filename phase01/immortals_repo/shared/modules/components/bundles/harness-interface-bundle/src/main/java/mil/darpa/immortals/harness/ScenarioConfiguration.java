package mil.darpa.immortals.harness;

import mil.darpa.immortals.harness.configuration.targets.ClientConfiguration;
import mil.darpa.immortals.harness.configuration.targets.ServerConfiguration;

import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 11/1/16.
 */
public class ScenarioConfiguration {
    public String sessionIdentifier;
    public ServerConfiguration server;
    public final LinkedList<ClientConfiguration> clients = new LinkedList<>();

}
