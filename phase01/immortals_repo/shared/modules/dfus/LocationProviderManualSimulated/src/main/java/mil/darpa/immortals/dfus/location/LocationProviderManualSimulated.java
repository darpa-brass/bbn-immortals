package mil.darpa.immortals.dfus.location;

import android.content.Context;
import android.os.Environment;
import com.google.gson.Gson;
import com.securboration.immortals.ontology.functionality.locationprovider.GetCurrentLocationAspect;
import com.securboration.immortals.ontology.functionality.locationprovider.InitializeAspect;
import com.securboration.immortals.ontology.functionality.locationprovider.LocationProvider;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.DfuAnnotation;
import mil.darpa.immortals.annotation.dsl.ontology.dfu.annotation.FunctionalAspectAnnotation;
import mil.darpa.immortals.core.analytics.Analytics;
import mil.darpa.immortals.core.analytics.AnalyticsEventType;
import mil.darpa.immortals.datatypes.Coordinates;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.ThreadLocalRandom;
import com.securboration.immortals.ontology.resources.UserInterface;

/**
 * Created by awellman@bbn.com on 2/12/16.
 */
@DfuAnnotation(
        functionalityBeingPerformed = LocationProvider.class,
        resourceDependencies = {
                UserInterface.class
        }
)
public class LocationProviderManualSimulated {

    private static final String providerIdentifier = "LocationProviderManualSimulated";

    private LocationProviderSimulatedImpl locationProvider;

    public LocationProviderManualSimulated() {
    }

    //    @SynthesisInit
    @FunctionalAspectAnnotation(aspect = InitializeAspect.class)
    public void initialize(Context context) {

        try {
            locationProvider = new LocationProviderSimulatedImpl(providerIdentifier, providerIdentifier + ".json");
        } catch (RuntimeException e) {
            Analytics.log(Analytics.newEvent(AnalyticsEventType.DfuMissmatchError, providerIdentifier, e.getMessage()));
            locationProvider = null;
        }
    }

    //    @SynthesisWork
//    @FunctionalDfuAspect(
//            functionalityUri = Semantics.Functionality_LocationProvider,
//            functionalAspectUri = Semantics.Functionality_LocationProvider_LastKnown
//    )
//    @SemanticTypeBinding(semanticType = Semantics.Datatype_Coordinates)
    @FunctionalAspectAnnotation(aspect = GetCurrentLocationAspect.class)
    public Coordinates getLastKnownLocation() {

        Coordinates result = null;

        if (locationProvider != null) {
            result = locationProvider.getCurrentLocation();
        }

        return result;

    }

    //////////// BEGIN LOCATIONPROVIDERSIMULATED COPY
    public static class LocationProviderSimulatedImpl {

        private final MockLocationBehaviorProfile behaviorProfile;

        private final String providerIdentifier;

        private final long startTime;

        public LocationProviderSimulatedImpl(@Nonnull String providerIdentifier, @Nonnull String profileFileName) {
            startTime = System.currentTimeMillis();
            this.providerIdentifier = providerIdentifier;

            MockLocationBehaviorProfile newProfile = null;
            // The use of this code indicates the "hardware" (file) is available.
            try {
                File inputFile = new File(Environment.getExternalStorageDirectory(), "ataklite/" + profileFileName);

//                if (inputFile.exists()) {
                FileReader fr = new FileReader(inputFile);
                Gson gson = new Gson();

                newProfile = gson.fromJson(fr, MockLocationBehaviorProfile.class);
//                }
            } catch (Exception e) {
                System.err.println("Unexpected exception: Requirements to use '" + providerIdentifier + "' have not been met!");
                throw new RuntimeException(e);
            } finally {
                behaviorProfile = newProfile;
            }
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

            return new Coordinates(latitude, longitude, null, null, currentTime, providerIdentifier);
        }

        public enum MockLocationCountry {

            UnitedStates(
                    30.755641,
                    48.562068,
                    -122.387100,
                    -81.490127
            ),
            Australia(
                    -31.250515,
                    -21.176879,
                    116.714491,
                    144.663708
            ),
            Argentina(
                    -38.176958,
                    -25.137360,
                    -68.123313,
                    -60.466707
            ),
            Russia(
                    56.914912,
                    65.924007,
                    42.534806,
                    131.480111
            );

            private final double minLat;
            private final double maxLat;
            private final double minLon;
            private final double maxLon;

            MockLocationCountry(double minLat, double maxLat, double minLon, double maxLon) {
                this.minLat = minLat;
                this.maxLat = maxLat;
                this.minLon = minLon;
                this.maxLon = maxLon;
            }

            public Coordinates getRandomLocation(@Nonnull String sourceIdentifier) {
                double latitude = getRandomLatitude();
                double longitude = getRandomLongitude();

                return new Coordinates(latitude, longitude, null, null, System.currentTimeMillis(), sourceIdentifier);
            }

            public double getRandomLatitude() {
                return ThreadLocalRandom.current().nextDouble(minLat, maxLat);
            }

            public double getRandomLongitude() {
                return ThreadLocalRandom.current().nextDouble(minLon, maxLon);
            }
        }

        public enum MockLocationDirection {
            North,
            East,
            South,
            West
        }

        public static class MockLocationBehaviorProfile {
            private Double initialLatitude;
            private Double initialLongitude;
            private MockLocationCountry country;
            private MockLocationDirection direction;
            private double degreeChangePerSecond;

            public MockLocationBehaviorProfile(@Nullable Double initialLatitude, @Nullable Double initialLongitude, @Nullable MockLocationCountry country, @Nonnull MockLocationDirection direction, @Nonnull double degreeChangePerSecond) {
                this.initialLatitude = initialLatitude;
                this.initialLongitude = initialLongitude;
                this.country = country;
                this.direction = direction;
                this.degreeChangePerSecond = degreeChangePerSecond;
            }

            public double getInitialLatitude() {
                if (initialLatitude == null) {
                    initialLatitude = country.getRandomLatitude();
                }

                return initialLatitude;
            }

            public double getInitialLongitude() {
                if (initialLongitude == null) {
                    initialLongitude = country.getRandomLongitude();
                }

                return initialLongitude;
            }

            public MockLocationDirection getDirection() {
                return direction;
            }

            public double getDegreeChangePerSecond() {
                return degreeChangePerSecond;
            }
        }
    }
    //////////// END LOCATIONPROVIDERSIMULATED COPY
}
