package mil.darpa.immortals.core.das.adaptationmodules.hddrass;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by awellman@bbn.com on 4/6/18.
 */
public class Hacks {

    private static final String MARTI_DM_IDENTIFIER = "http://darpa.mil/immortals/ontology/r2.0.0/cp2#ClientServerEnvironment.MartiServer";
    private static final String ATAKLITE_DM_IDENTIFIER = "http://darpa.mil/immortals/ontology/r2.0.0/cp2#ClientServerEnvironment.ClientDevice1";
    private static final String MARTI_COORDINATE_IDENTIFIER = "Marti";
    private static final String ATAKLITE_COORDINATE_IDENTIFIER = "ATAKLite";

    private static final Map<String, String> coordinatesToDeploymentModelMap = new HashMap<>();
    private static final Map<String, String> deploymentModelToCoordinatesMap = new HashMap<>();

    static {
        coordinatesToDeploymentModelMap.put(MARTI_COORDINATE_IDENTIFIER, MARTI_DM_IDENTIFIER);
        coordinatesToDeploymentModelMap.put(ATAKLITE_COORDINATE_IDENTIFIER, ATAKLITE_DM_IDENTIFIER);
        deploymentModelToCoordinatesMap.put(MARTI_DM_IDENTIFIER, MARTI_COORDINATE_IDENTIFIER);
        deploymentModelToCoordinatesMap.put(ATAKLITE_DM_IDENTIFIER, ATAKLITE_COORDINATE_IDENTIFIER);
    }

    // TODO: This linkage shouldn't need this hack...
    public static String deploymentModelIdentifierToAbsoluteIdentifier(String deploymentModelIdentifier) {
        return deploymentModelToCoordinatesMap.get(deploymentModelIdentifier);
    }

    // TODO: This linkage shouldn't need this hack...
    public static String absoluteIdentifierToDeploymentModelIdentifier(String coordinates) {
        return coordinatesToDeploymentModelMap.get(coordinates);
    }
}
