package com.securboration.immortals.ontology.functionality.datatype;

/**
 * Representation of a physical location
 * 
 * @author Securboration
 *
 */
public class Location extends DataType{
    
    /**
     * The latitude in degrees
     */
    private double latitude;
    
    /**
     * The longitude in degrees
     */
    private double longitude;
    
    /**
     * The altitude above the WGS84 ellipsoid
     */
    private double altitudeWGS84;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitudeWGS84() {
        return altitudeWGS84;
    }

    public void setAltitudeWGS84(double altitudeWGS84) {
        this.altitudeWGS84 = altitudeWGS84;
    }

}
