package mil.darpa.immortals.dfus.location;

import mil.darpa.immortals.core.simulated.EnvironmentConfiguration;
import mil.darpa.immortals.core.simulated.SimulatedLocation;
import mil.darpa.immortals.datatypes.Coordinates;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 2/12/16.
 */
public class JavaTemporaryLocationProviderManualSimulated {

    private static final String PROFILE_IDENTIFIER = "LocationProviderManualSimulated";
    private static final String HOW = "h-e-s";

    private LocationProviderSimulatedImpl locationProvider;

    public JavaTemporaryLocationProviderManualSimulated() {
    }

    public void initialize() {
        SimulatedLocation.BehaviorProfile behaviorProfile =
                EnvironmentConfiguration.getAndroidEnvironment().tryGetLocationProviderProfile(PROFILE_IDENTIFIER);
        locationProvider = new LocationProviderSimulatedImpl(HOW, behaviorProfile);
    }

    public Coordinates getLastKnownLocation() {

        Coordinates result = null;

        if (locationProvider != null) {
            result = locationProvider.getCurrentLocation();
        }

        return result;

    }

    //////////// BEGIN LOCATIONPROVIDERSIMULATED COPY
    public static class LocationProviderSimulatedImpl {

        private final SimulatedLocation.BehaviorProfile behaviorProfile;

        private final String how;

        private final long startTime;

        public LocationProviderSimulatedImpl(@Nonnull String how, @Nonnull SimulatedLocation.BehaviorProfile behaviorProfile) {
            startTime = System.currentTimeMillis();
            this.how = how;

            this.behaviorProfile = behaviorProfile;
        }

        public Coordinates getCurrentLocation() {
            long currentTime = System.currentTimeMillis();
            long timeDelta = (currentTime - startTime) / 1000;
            double travelDistance = timeDelta * behaviorProfile.getDegreeChangePerSecond();

            double latitude = 0;
            double longitude = 0;

            switch (behaviorProfile.getDirection()) {
                case North:
                    latitude = behaviorProfile.getInitialLatitude() + travelDistance;
                    longitude = behaviorProfile.getInitialLongitude();
                    break;

                case East:
                    latitude = behaviorProfile.getInitialLatitude();
                    longitude = behaviorProfile.getInitialLongitude() + travelDistance;
                    break;

                case South:
                    latitude = behaviorProfile.getInitialLatitude() - travelDistance;
                    longitude = behaviorProfile.getInitialLongitude();
                    break;

                case West:
                    latitude = behaviorProfile.getInitialLatitude();
                    longitude = behaviorProfile.getInitialLongitude() - travelDistance;
                    break;
            }

            return new Coordinates(latitude, longitude, null, null, currentTime, how);
        }
    }
    //////////// END LOCATIONPROVIDERSIMULATED COPY
}
