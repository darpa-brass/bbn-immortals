package mil.darpa.immortals.core.simulated;

import com.google.gson.Gson;
import mil.darpa.immortals.core.analytics.Analytics;
import mil.darpa.immortals.core.analytics.AnalyticsEventType;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Defines simulated aspects defined for the IMMoRTALS systems
 * <p>
 * Created by awellman@bbn.com on 1/9/17.
 */
public class EnvironmentConfiguration {

    public enum RESOURCES {
        BLUETOOTH("bluetooth"),
        USB("usb"),
        INTERNAL_GPS("internalGps"),
        USER_INTERFACE("userInterface"),
        GPS_SATELLITES("gpsSatellites");

        private final String identifier;

        RESOURCES(String identifier) {
            this.identifier = identifier;
        }

        public String getIdentifier() {
            return identifier;
        }
    }

    public enum DfuResourceMapping {
        LocationProviderBluetoothGpsSimulated(RESOURCES.BLUETOOTH, RESOURCES.GPS_SATELLITES),
        LocationProviderUsbGpsSimulated(RESOURCES.USB, RESOURCES.GPS_SATELLITES),
        LocationProviderManualSimulated(RESOURCES.USER_INTERFACE),
        LocationProviderSaasmSimulated(RESOURCES.USB, RESOURCES.GPS_SATELLITES),
        LocationProviderAndroidGpsBuiltIn(RESOURCES.GPS_SATELLITES);

        private final RESOURCES[] necessaryResources;

        DfuResourceMapping(RESOURCES... resources) {
            this.necessaryResources = resources;
        }
    }


    private static final String androidEnvironmentFilepathA = "/storage/sdcard/ataklite/env.json";
    private static final String androidEnvironmentFilepathB = "/storage/emulated/0/ataklite/env.json";
    private static final String javaEnvironmentFilepathA = "clitak/env.json";

    private static EnvironmentConfiguration simulatedEnvironment;

    private LinkedList<String> availableResources;

    public HashMap<String, SimulatedLocation.BehaviorProfile> locationBehaviorProfiles;

    public EnvironmentConfiguration(List<String> availableResources, Map<String, SimulatedLocation.BehaviorProfile> locationBehaviorProfiles) {
        this.availableResources = new LinkedList<>(availableResources);
        this.locationBehaviorProfiles = new HashMap<>(locationBehaviorProfiles);
    }

    public static EnvironmentConfiguration getAndroidEnvironment() {
        try {
            if (simulatedEnvironment == null) {
                File inputFile = new File(androidEnvironmentFilepathA);

                if (inputFile.exists()) {
                    FileReader fr = new FileReader(inputFile);
                    simulatedEnvironment = new Gson().fromJson(fr, EnvironmentConfiguration.class);
                } else {
                    inputFile = new File(androidEnvironmentFilepathB);
                    if (inputFile.exists()) {
                        FileReader fr = new FileReader(inputFile);
                        simulatedEnvironment = new Gson().fromJson(fr, EnvironmentConfiguration.class);

                    } else {
                        inputFile = new File(javaEnvironmentFilepathA);
                        if (inputFile.exists()) {
                            FileReader fr = new FileReader(inputFile);
                            simulatedEnvironment = new Gson().fromJson(fr, EnvironmentConfiguration.class);
                        } else{
                            throw new RuntimeException("Attempt to get simulated environment configuration file failed!");
                        }
                    }
                }
            }

            return simulatedEnvironment;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SimulatedLocation.BehaviorProfile tryGetLocationProviderProfile(String profileIdentifier) {
        handleMissingResources(profileIdentifier);
        SimulatedLocation.BehaviorProfile profile = locationBehaviorProfiles.get(profileIdentifier);

        if (profile == null) {
            throw new RuntimeException("No simulated GPS profile is defined for \"" + profileIdentifier + "\"!");
        } else {
            return profile;
        }
    }

    public void handleMissingResources(String profileIdentifier) {
        RESOURCES[] necessaryResources = DfuResourceMapping.valueOf(profileIdentifier).necessaryResources;

        for (RESOURCES r : necessaryResources) {
            if (availableResources == null) {
                String msg = "No simulated environment configured so necessary resource \"" + r.getIdentifier() + "\" was not found!";
                Analytics.log(Analytics.newEvent(AnalyticsEventType.DfuMissmatchError, profileIdentifier, msg));
                throw new RuntimeException(msg);

            } else if (!availableResources.contains(r.getIdentifier())) {
                String msg = "Necessary resource \"" + r.getIdentifier() + "\" was not found in provided resources!";
                Analytics.log(Analytics.newEvent(AnalyticsEventType.DfuMissmatchError, profileIdentifier, msg));
                throw new RuntimeException(msg);
            }
        }
    }
}
