package mil.darpa.immortals.dfus;

/**
 * Created by awellman@bbn.com on 2/12/18.
 */
public class ElevationData {

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getHae() {
        return hae;
    }

    public double getLe() {
        return le;
    }

    private final double latitude;
    private final double longitude;
    private final double hae;
    private final double le;

    public ElevationData(double latitude, double longitude, double hae, double le) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.hae = hae;
        this.le = le;
    }

}
