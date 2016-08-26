package mil.darpa.immortals.javatypeconverters;

import android.location.Location;
import mil.darpa.immortals.datatypes.Coordinates;

import javax.annotation.Nonnull;

/**
 * Created by awellman@bbn.com on 2/15/16.
 */
public class CoordinateLocationConverter {

    public static Coordinates toCoordinates(@Nonnull Location location) {
        Coordinates coordinates = new Coordinates(
                location.getLatitude(),
                location.getLongitude(),
                (location.hasAltitude() ? location.getAltitude() : null),
                (location.hasAccuracy() ? location.getAccuracy() : null),
                location.getTime(),
                location.getProvider()
        );

        return coordinates;
    }

    public static Location toLocation(@Nonnull Coordinates coordinates) {
        Location location = new Location("Coordinates");

        location.setLatitude(coordinates.getLatitude());
        location.setLongitude(coordinates.getLongitude());
        location.setTime(coordinates.getAcquisitionTime());
        location.setProvider(coordinates.getProvider());

        if (coordinates.hasAltitude()) {
            location.setAltitude(coordinates.getAltitudeMSL());
        }

        if (coordinates.hasAccuracy()) {
            location.setAccuracy(coordinates.getAccuracyMetric());
        }

        return location;
    }
}
