package mil.darpa.immortals.applications.clitak;

import mil.darpa.immortals.core.simulated.SimulatedLocation;
import mil.darpa.immortals.datatypes.Coordinates;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 7/24/17.
 */
public class LocationProviderSimulatedImpl {

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
