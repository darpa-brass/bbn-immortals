package mil.darpa.immortals.core.simulated;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by awellman@bbn.com on 1/9/17.
 */
public class SimulatedLocation {

    public enum Country {

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

        Country(double minLat, double maxLat, double minLon, double maxLon) {
            this.minLat = minLat;
            this.maxLat = maxLat;
            this.minLon = minLon;
            this.maxLon = maxLon;
        }

        public double getRandomLatitude() {
            return ThreadLocalRandom.current().nextDouble(minLat, maxLat);
        }

        public double getRandomLongitude() {
            return ThreadLocalRandom.current().nextDouble(minLon, maxLon);
        }
    }

    public enum LocationDirection {
        North,
        East,
        South,
        West
    }

    public static class BehaviorProfile {
        private Double initialLatitude;
        private Double initialLongitude;
        private Country country;
        private LocationDirection direction;
        private double degreeChangePerSecond;

        public BehaviorProfile(@Nullable Double initialLatitude, @Nullable Double initialLongitude, @Nullable SimulatedLocation.Country country, @Nonnull LocationDirection direction, @Nonnull double degreeChangePerSecond) {
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

        public LocationDirection getDirection() {
            return direction;
        }

        public double getDegreeChangePerSecond() {
            return degreeChangePerSecond;
        }
    }
}
