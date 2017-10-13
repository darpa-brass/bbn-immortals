package mil.darpa.immortals.dfus.location;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import com.securboration.immortals.ontology.functionality.locationprovider.GetCurrentLocationAspect;
import com.securboration.immortals.ontology.functionality.locationprovider.InitializeAspect;
import com.securboration.immortals.ontology.functionality.locationprovider.LocationProvider;
import com.securboration.immortals.ontology.resources.BluetoothResource;
import com.securboration.immortals.ontology.resources.gps.GpsSatelliteConstellation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;
import mil.darpa.immortals.core.analytics.Analytics;
import mil.darpa.immortals.core.analytics.AnalyticsEventType;
import mil.darpa.immortals.core.simulated.EnvironmentConfiguration;
import mil.darpa.immortals.core.simulated.SimulatedLocation;
import mil.darpa.immortals.core.synthesis.annotations.dfu.SynthesisAndroidContext;
import mil.darpa.immortals.datatypes.Coordinates;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 2/12/16.
 */
@DfuAnnotation(
        functionalityBeingPerformed = LocationProvider.class,
        resourceDependencies = {
                GpsSatelliteConstellation.class,
                BluetoothResource.class
        }
)
public class LocationProviderBluetoothGpsSimulated {

    private static final String PROFILE_IDENTIFIER = "LocationProviderBluetoothGpsSimulated";
    private static final String HOW = "m-g-s-b";

    private LocationProviderSimulatedImpl locationProvider;

    public LocationProviderBluetoothGpsSimulated() {
    }

    @FunctionalAspectAnnotation(aspect = InitializeAspect.class)
    public void initialize(@SynthesisAndroidContext Context context) {
        SimulatedLocation.BehaviorProfile behaviorProfile =
                EnvironmentConfiguration.getAndroidEnvironment().tryGetLocationProviderProfile(PROFILE_IDENTIFIER);
        locationProvider = new LocationProviderSimulatedImpl(HOW, behaviorProfile);

        try {
            final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            for (BluetoothDevice bluetoothDevice : bluetoothAdapter.getBondedDevices()) {
                bluetoothDevice.getAddress();
            }
        } catch (Exception e) {
            Analytics.log(Analytics.newEvent(AnalyticsEventType.UnexepctedIgnorableError, PROFILE_IDENTIFIER, "Error iterating over bluetoothdevices!"));
        }
    }

    @FunctionalAspectAnnotation(aspect = GetCurrentLocationAspect.class)
    public Coordinates getCurrentLocation() {

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
