package mil.darpa.immortals.datatypes;


import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Coordinates {

    private final double latitude;
    private final double longitude;
    private final double altitude;
    private final float accuracy;
    private final long acquisitionTime;
    private final boolean hasAltitude;
    private final boolean hasAccuracy;

    private final String provider;

    public Coordinates(double latitude, double longitude, @Nullable Double altitude, @Nullable Float accuracy, long acquisitionTime, @Nonnull String provider) {
        this.latitude = latitude;
        this.longitude = longitude;

        this.hasAltitude = (altitude != null);
        this.altitude = (hasAltitude ? altitude : 0);

        this.hasAccuracy = (accuracy != null);
        this.accuracy = (hasAccuracy ? accuracy : 0);

        this.acquisitionTime = acquisitionTime;
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }

    public boolean hasAltitude() {
        return hasAltitude;
    }

    public boolean hasAccuracy() {
        return hasAccuracy;
    }

    /**
     * @return the measured latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @return the measured longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @return the measured altitude in meters above mean sea level
     */
    public double getAltitudeMSL() {
        return altitude;
    }

    /**
     * @return the GDOP metric for the GPS signal. See <a href=
     * "https://en.wikipedia.org/wiki/Dilution_of_precision_%28GPS%29">
     * Dilution of Precision (DOP)</a>
     */
    // The ontology will contain additional triples identifying GDOP as a type
    // of metric for location performance. Somewhere, an application-specific
    // trigger condition must be specified (e.g., if GDOP exceeds 3.5, seek an
    // alternative mechanism for location). Probably doesn't make sense to do
    // this in the code itself but rather in some artifact that exists alongside
    // the code and is specific to a specific set of performance constraints.
    public float getAccuracyMetric() {
        return accuracy;
    }

    /**
     * @return the timestamp of the location reading
     */
    // The ontology will contain additional triples identifying acquisition time
    // offset as a metric for location performance. We could also have an age
    // limit that triggers a new location provider being swapped in.
    public long getAcquisitionTime() {
        return acquisitionTime;
    }
}
